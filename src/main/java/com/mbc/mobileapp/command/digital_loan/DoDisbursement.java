package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.api.ApiFundsTransfer;
import com.mbc.common.api.models.fundsTransfer.*;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlLoanDisbursement;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLoanDisbursementRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.RedisServer;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.SalaryAdvanceConstant;
import com.mbc.mobileapp.constant.ServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * DoDisbursement — Bước chuyển tiền sau khi MS LOAN đã tạo LD thành công.
 *
 * <p>Luồng xử lý theo BRS mục 6.2 bước 36.2 → 45:
 * <pre>
 * Chặng 2 (bắt buộc, mọi trường hợp):
 *   INHOUSE: drawdownAccount (Working Account) → Current Account của KH
 *   → Lưu t24Ft vào COM_TRANS + COM_TRANS_DTL_LOAN_DISBURSEMENT + COM_TRANS_PROCESS
 *
 * Chặng 3 (chỉ khi disbursementType = "EMONEY_WALLET"):
 *   CIFTP: Current Account KH → Ví eMoney (qua Bakong liên ngân hàng)
 *   → Retry tối đa 3 lần nếu timeout (soaErrorCode = "002" hoặc null response)
 *   → Lưu transHash vào context ("ft_trans_hash") để DoPushEmoneyLoan dùng đối soát
 *   → Nếu sau 3 lần retry vẫn fail: ghi PND, FE hiển thị thông báo chuyển tay
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoDisbursement implements Command {

    private final ApiFundsTransfer apiFundsTransfer;
    private final ComTransRepo comTransRepo;
    private final ComTransDtlLoanDisbursementRepo comTransDtlLoanDisbursementRepo;
    private final ComTransProcessRepo comTransProcessRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();

        try {
            // ── Load ComTrans và ComTransDtlLoanDisbursement ──────────────────
            ComTrans comTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(
                    custInfo.getId(),
                    request.getSrvcCd(),
                    Constant.COM_STATUS_INT,
                    request.getTransId(),
                    request.getSessionId());

            if (comTrans == null) {
                log.error("[DoDisbursement] ComTrans not found - transId:{}, requestId:{}",
                        request.getTransId(), request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return true;
            }

            ComTransDtlLoanDisbursement dtl =
                    comTransDtlLoanDisbursementRepo.findById(comTrans.getId()).orElse(null);
            if (dtl == null) {
                log.error("[DoDisbursement] DtlLoanDisbursement not found - id:{}, requestId:{}",
                        comTrans.getId(), request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return true;
            }

            // ── Lấy disbursementType từ request ──────────────────────────────
            DisbursementRequest disbReq = request.getDisbursementRequest();
            String disbursementType = disbReq != null ? disbReq.getDisbursementType() : null;

            // ═════════════════════════════════════════════════════════════════
            // CHẶNG 2: INHOUSE — drawdownAccount → Current Account KH
            // ═════════════════════════════════════════════════════════════════
            log.info("[DoDisbursement] Chặng 2 INHOUSE bắt đầu - requestId:{}", request.getRequestId());

            ExecuteT24Output<DataOutput> inhouseOutput =
                    executeInhouseTransfer(comTrans, dtl, custInfo.getId(), request.getRequestId());

            if (inhouseOutput == null) {
                // Timeout hoàn toàn
                persistState(comTrans, dtl, Constant.COM_STATUS_PND, null, null, null, "Chặng 2 INHOUSE timeout");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                        ResponseCode.REQUEST_TIMEOUT.getCode());
                context.setResult(result);
                context.setResponse(response);
                return true;
            }

            if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(inhouseOutput.getStatus())) {
                // Lỗi INHOUSE
                String errCode = inhouseOutput.getErrorInfo() != null
                        ? inhouseOutput.getErrorInfo().getErrorCode() : ResponseCode.TRANSACTION_FAIL.getCode();
                String errDesc = buildErrDesc(inhouseOutput);

                String persistStatus = "002".equals(inhouseOutput.getSoaErrorCode())
                        ? Constant.COM_STATUS_PND : Constant.COM_STATUS_FAIL;
                persistState(comTrans, dtl, persistStatus, null, errCode, errDesc, "Chặng 2 INHOUSE lỗi");

                if ("002".equals(inhouseOutput.getSoaErrorCode())) {
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());
                } else {
                    result = new SimpleResult(errDesc, false, errCode);
                }
                context.setResult(result);
                context.setResponse(response);
                return true;
            }

            // Chặng 2 thành công
            String ft2 = inhouseOutput.getData().getT24Ft();
            log.info("[DoDisbursement] Chặng 2 INHOUSE thành công - t24Ft:{}, requestId:{}", ft2, request.getRequestId());
            persistState(comTrans, dtl, Constant.COM_STATUS_COM, ft2, null, null, "Chặng 2 INHOUSE OK");

            // Set FT lên response (FE dùng hiển thị màn thành công)
            response.setFt(ft2);

            // ── Xóa Redis cache danh sách loan ─────────────────────────────
            try {
                RedisServer.removeCacheRedis(
                        ServiceConstant.REDIS_KEY_LIST_LOAN + request.getSessionId());
            } catch (Exception re) {
                log.warn("[DoDisbursement] Redis clear failed (non-blocking): {}", re.getMessage());
            }

            // ═════════════════════════════════════════════════════════════════
            // CHẶNG 3: CIFTP — Chỉ thực hiện khi KH chọn giải ngân vào ví eM
            // ═════════════════════════════════════════════════════════════════
            if (SalaryAdvanceConstant.DISBURSEMENT_TYPE_EMONEY_WALLET.equals(disbursementType)) {
                log.info("[DoDisbursement] Chặng 3 CIFTP bắt đầu - requestId:{}", request.getRequestId());

                // Lấy thông tin Current Account KH (vừa nhận tiền ở Chặng 2)
                // creditAccount của Chặng 2 chính là debitAccount của Chặng 3
                String currentAcctNo   = dtl.getCrebitAcctNo();
                String currentAcctName = dtl.getCrebitAcctName();
                String currentAcctCcy  = dtl.getCrebitAcctCcy();

                // creditAccount Chặng 3 = ví eMoney của KH
                // Lấy từ context "em_wallet_number" do DoCheckDisbursementAccount hoặc FE set
                String emoneyWalletNo   = getStringFromContext(context, "em_wallet_number");
                String emoneyWalletName = getStringFromContext(context, "em_wallet_name");
                if (Utility.isNull(emoneyWalletNo)) {
                    // fallback: lấy từ request nếu FE gửi trực tiếp
                    emoneyWalletNo = disbReq != null ? disbReq.getSelectedAccountNumber() : null;
                }

                if (Utility.isNull(emoneyWalletNo)) {
                    // Không có số ví → không block chain, FE đọc ciftp_status để hiển thị thông báo chuyển tay
                    log.error("[DoDisbursement] Chặng 3: emoneyWalletNo null - requestId:{}", request.getRequestId());
                    context.put("ciftp_status", "FAIL_NO_WALLET");

                } else {
                    // Có số ví → gọi CIFTP với retry
                    ExecuteT24Output<DataOutput> ciftpOutput = executeCiftpWithRetry(
                            comTrans,
                            currentAcctNo, currentAcctName, currentAcctCcy,
                            emoneyWalletNo, Utility.isNull(emoneyWalletName) ? currentAcctName : emoneyWalletName,
                            currentAcctCcy,
                            dtl.getDebitAmount() != null ? dtl.getDebitAmount().toPlainString() : "0",
                            custInfo.getId(),
                            request.getRequestId(),
                            SalaryAdvanceConstant.CIFTP_MAX_RETRY);

                    if (ciftpOutput != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(ciftpOutput.getStatus())) {
                        // Chặng 3 thành công
                        String ft3       = ciftpOutput.getData().getT24Ft();
                        String transHash = ciftpOutput.getData().getTransHash();
                        log.info("[DoDisbursement] Chặng 3 CIFTP thành công - t24Ft:{}, transHash:{}, requestId:{}",
                                ft3, transHash, request.getRequestId());

                        // Lưu transHash vào context để DoPushEmoneyLoan dùng đối soát
                        context.put("ft_trans_hash", transHash);
                        context.put("ciftp_ft", ft3);
                        context.put("ciftp_status", "SUCCESS");

                        // Ghi log Chặng 3 vào COM_TRANS_PROCESS (không ghi đè STATUS COM của Chặng 2)
                        saveProcess(comTrans, "CIFTP_" + ft3, Constant.COM_STATUS_COM, null, null);

                    } else {
                        // Chặng 3 fail sau tất cả retry
                        String errCode = (ciftpOutput != null && ciftpOutput.getErrorInfo() != null)
                                ? ciftpOutput.getErrorInfo().getErrorCode() : "TIMEOUT";
                        String errDesc = ciftpOutput != null ? buildErrDesc(ciftpOutput)
                                : "Chặng 3 CIFTP timeout sau " + SalaryAdvanceConstant.CIFTP_MAX_RETRY + " lần retry";
                        log.error("[DoDisbursement] Chặng 3 CIFTP thất bại - errCode:{}, requestId:{}", errCode, request.getRequestId());

                        context.put("ciftp_status", "FAIL");

                        // Ghi PND vào PROCESS để Ops tra soát; Chặng 2 STATUS vẫn là COM
                        // BRS bước 45 TH2: FE đọc ciftp_status = "FAIL" → hiển thị thông báo cho KH
                        saveProcess(comTrans, null, Constant.COM_STATUS_PND, errCode, errDesc);
                    }
                }
            }

            // Chặng 2 thành công → chain tiếp tục sang DoPushEmoneyLoan
            context.setResult(result);
            context.setResponse(response);
            return false;

        } catch (Exception e) {
            log.error("[DoDisbursement] Exception - requestId:{}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
            context.setResult(result);
            return true;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHẶNG 2: INHOUSE — Working Account → Current Account KH
    // ─────────────────────────────────────────────────────────────────────────

    private ExecuteT24Output<DataOutput> executeInhouseTransfer(
            ComTrans comTrans,
            ComTransDtlLoanDisbursement dtl,
            String custId,
            String requestId) throws Exception {

        FundsTransferInfo transferInfo = new FundsTransferInfo();

        // Credit = Current Account KH (nhận tiền giải ngân)
        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setAccountName(dtl.getCrebitAcctName());
        creditAccount.setAccountNumber(dtl.getCrebitAcctNo());
        creditAccount.setAccountCurrency(dtl.getCrebitAcctCcy());
        creditAccount.setAccountType("ACCOUNT");

        // Debit = Working Account (drawdownAccount từ MS LOAN)
        DebitAccount debitAccount = new DebitAccount();
        debitAccount.setAccountNumber(dtl.getDebitAcctNo());
        debitAccount.setAccountName(dtl.getDebitAcctName());
        debitAccount.setAccountCurrency(dtl.getDebitAcctCcy());
        debitAccount.setAccountType("ACCOUNT");

        Amount amount = new Amount();
        amount.setAmount(dtl.getDebitAmount());
        amount.setCurrency(dtl.getDebitAcctCcy());

        List<AddInfoList> addInfoList = new ArrayList<>();
        AddInfoList orderingBank = new AddInfoList();
        orderingBank.setName("ORDERING_BANK");
        orderingBank.setValue("MBC");
        addInfoList.add(orderingBank);

        transferInfo.setCreditAccount(creditAccount);
        transferInfo.setDebitAccount(debitAccount);
        transferInfo.setAmount(amount);
        transferInfo.setCreditBank(new CreditBank());
        transferInfo.setBranchCode(dtl.getBranchCode());
        transferInfo.setRemark(comTrans.getDescription());
        transferInfo.setTransferType(SalaryAdvanceConstant.TRANSFER_TYPE_INHOUSE);
        transferInfo.setAddInfoList(addInfoList);

        return apiFundsTransfer.makeTransfer(transferInfo, custId, requestId, comTrans.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHẶNG 3: CIFTP — Current Account KH → Ví eMoney (với retry)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Gọi MS FT CIFTP và retry tối đa {@code maxRetry} lần khi timeout.
     *
     * <p>BRS bước 44–45:
     * <ul>
     *   <li>HttpStatus 409 + soaErrorCode 404 (trùng transactionId) → coi là thành công</li>
     *   <li>HttpStatus 200 → thành công</li>
     *   <li>Còn lại timeout/PND → retry tiếp; sau hết retry trả về output cuối</li>
     * </ul>
     */
    private ExecuteT24Output<DataOutput> executeCiftpWithRetry(
            ComTrans comTrans,
            String debitAcctNo, String debitAcctName, String debitAcctCcy,
            String creditAcctNo, String creditAcctName, String creditAcctCcy,
            String amount,
            String custId,
            String requestId,
            int maxRetry) throws Exception {

        FundsTransferInfo transferInfo = buildCiftpTransferInfo(
                comTrans,
                debitAcctNo, debitAcctName, debitAcctCcy,
                creditAcctNo, creditAcctName, creditAcctCcy,
                amount);

        ExecuteT24Output<DataOutput> output = null;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            log.info("[DoDisbursement] Chặng 3 CIFTP attempt {}/{} - requestId:{}", attempt, maxRetry, requestId);
            output = apiFundsTransfer.makeTransfer(transferInfo, custId, requestId, comTrans.getId());

            if (output == null) {
                log.warn("[DoDisbursement] Chặng 3 attempt {} timeout (null response) - requestId:{}", attempt, requestId);
                // Tiếp tục retry
                continue;
            }

            // BRS: HttpStatus 200 → thành công
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                log.info("[DoDisbursement] Chặng 3 thành công ở attempt {} - requestId:{}", attempt, requestId);
                return output;
            }

            // BRS: HttpStatus 409 + soaErrorCode 404 (duplicate transId) → coi như đã hạch toán
            if ("409".equals(output.getStatus()) && "404".equals(output.getSoaErrorCode())) {
                log.info("[DoDisbursement] Chặng 3 duplicate transId (409/404) - coi như thành công, attempt:{}, requestId:{}",
                        attempt, requestId);
                return output; // FE xử lý cùng như success
            }

            // Lỗi timeout SOA "002" → retry
            if ("002".equals(output.getSoaErrorCode())) {
                log.warn("[DoDisbursement] Chặng 3 attempt {} soaErrorCode=002, retry... requestId:{}", attempt, requestId);
                continue;
            }

            // Lỗi khác (không phải timeout) → không retry, dừng ngay
            log.error("[DoDisbursement] Chặng 3 lỗi nghiệp vụ ở attempt {} - status:{}, soaCode:{}, requestId:{}",
                    attempt, output.getStatus(), output.getSoaErrorCode(), requestId);
            return output;
        }

        log.error("[DoDisbursement] Chặng 3 CIFTP hết {} lần retry - requestId:{}", maxRetry, requestId);
        return output; // null hoặc output lỗi cuối cùng
    }

    private FundsTransferInfo buildCiftpTransferInfo(
            ComTrans comTrans,
            String debitAcctNo, String debitAcctName, String debitAcctCcy,
            String creditAcctNo, String creditAcctName, String creditAcctCcy,
            String amountStr) {

        FundsTransferInfo transferInfo = new FundsTransferInfo();

        // Debit = Current Account KH (nguồn tiền sau Chặng 2)
        DebitAccount debitAccount = new DebitAccount();
        debitAccount.setAccountNumber(debitAcctNo);
        debitAccount.setAccountName(debitAcctName);
        debitAccount.setAccountCurrency(debitAcctCcy);
        debitAccount.setAccountType("ACCOUNT");

        // Credit = Ví eMoney của KH (WALLET type)
        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setAccountNumber(creditAcctNo);
        creditAccount.setAccountName(creditAcctName);
        creditAccount.setAccountCurrency(creditAcctCcy);
        creditAccount.setAccountType("ACCOUNT"); // đối với CIFTP, accountType vẫn = ACCOUNT

        // BenAccount = Số ví eMoney (accountType = WALLET theo BRS)
        BenAccount benAccount = new BenAccount();
        benAccount.setAccountNumber(creditAcctNo);
        benAccount.setAccountName(creditAcctName);
        benAccount.setAccountType("WALLET");
        benAccount.setAccountCurrency(creditAcctCcy);

        // Parse amountStr → BigDecimal một lần, dùng chung cho benAccount + amount
        java.math.BigDecimal amountBD;
        try {
            amountBD = new java.math.BigDecimal(amountStr);
        } catch (Exception e) {
            amountBD = java.math.BigDecimal.ZERO;
        }

        benAccount.setBenAmount(String.valueOf(amountBD));

        Amount amount = new Amount();
        amount.setAmount(amountBD);
        amount.setCurrency(debitAcctCcy);
        amount.setBenAmount(amountBD);
        amount.setBenCurrency(creditAcctCcy);


        List<AddInfoList> addInfoList = new ArrayList<>();
        AddInfoList orderingBank = new AddInfoList();
        orderingBank.setName("ORDERING_BANK");
        orderingBank.setValue("MBC");
        addInfoList.add(orderingBank);

        AddInfoList orgData = new AddInfoList();
        orgData.setName("ORG.DATA");
        orgData.setValue("SENDER");
        addInfoList.add(orgData);

        transferInfo.setDebitAccount(debitAccount);
        transferInfo.setCreditAccount(creditAccount);
        transferInfo.setBenAccount(benAccount);
        transferInfo.setAmount(amount);
        transferInfo.setCreditBank(new CreditBank());
        transferInfo.setBranchCode(null); // SDK BE dùng branchCode của customer
        transferInfo.setRemark(comTrans.getDescription()); // transactionId làm remark (BRS 6.5.8)
        transferInfo.setTransferType(SalaryAdvanceConstant.TRANSFER_TYPE_CIFTP);
        transferInfo.setAddInfoList(addInfoList);

        return transferInfo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DB PERSISTENCE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cập nhật trạng thái COM_TRANS, COM_TRANS_DTL_LOAN_DISBURSEMENT và
     * ghi một dòng audit vào COM_TRANS_PROCESS.
     */
    private void persistState(
            ComTrans comTrans,
            ComTransDtlLoanDisbursement dtl,
            String status,
            String ft,
            String errCode,
            String errDesc,
            String step) {

        // COM_TRANS
        comTrans.setStatus(status);
        if (!Utility.isNull(ft)) comTrans.setFt(ft);
        comTransRepo.saveAndFlush(comTrans);

        // COM_TRANS_DTL_LOAN_DISBURSEMENT
        dtl.setStatus(status);
        if (!Utility.isNull(ft)) dtl.setFt(ft);
        comTransDtlLoanDisbursementRepo.saveAndFlush(dtl);

        // COM_TRANS_PROCESS
        saveProcess(comTrans, ft, status, errCode, errDesc);

        log.info("[DoDisbursement] persistState step:{}, status:{}, ft:{}", step, status, ft);
    }

    /** Ghi một dòng vào COM_TRANS_PROCESS (audit trail). */
    private void saveProcess(ComTrans comTrans, String ft, String status, String errCode, String errDesc) {
        ComTransProcess proc = new ComTransProcess();
        proc.setSrvcCd(comTrans.getSrvcCd());
        proc.setTransId(comTrans.getId());
        proc.setStatus(status);
        if (!Utility.isNull(ft)) proc.setFt(ft);
        if (!Utility.isNull(errCode)) proc.setErrorCode(errCode);
        if (!Utility.isNull(errDesc)) proc.setErrorDesc(errDesc);
        comTransProcessRepo.saveAndFlush(proc);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITY
    // ─────────────────────────────────────────────────────────────────────────

    private String buildErrDesc(ExecuteT24Output<DataOutput> output) {
        if (output == null || output.getErrorInfo() == null) return "Unknown error";
        String desc = output.getErrorInfo().getErrorDesc();
        String detail = output.getErrorInfo().getErrorDetail();
        return (!Utility.isNull(detail)) ? desc + " - " + detail : desc;
    }

    private String getStringFromContext(ProcessContext context, String key) {
        Object val = context.get(key);
        return (val instanceof String) ? (String) val : null;
    }
}
