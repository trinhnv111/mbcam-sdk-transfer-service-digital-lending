package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.api.ApiFundsTransfer;
import com.mbc.common.api.models.fundsTransfer.*;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlLoanDisbursement;
import com.mbc.common.entity.ComTransDtlLoanRegistration;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLoanDisbursementRepo;
import com.mbc.common.repository.ComTransDtlLoanRegistrationRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.RedisServer;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.ServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.services.il.nonsavingacct.PostingRestrict;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Step: Make Transfer — Đẩy tiền từ Working Account về tài khoản đích của KH
 *
 * TH1 (MBC_ACCOUNT):   Working Account → Tài khoản thanh toán MBC (INHOUSE)
 * TH2 (EMONEY_WALLET): 2 bước:
 *   Bước 2.1: Working Account → Current Account MBC (INHOUSE)
 *   Bước 2.2: Current Account MBC → Ví eMoney (CIFTP, retry 3 lần nếu timeout)
 *
 * Input từ context:
 *   - "drawdown_account"    → working account giải ngân (từ DoCreateLoan)
 *   - "loan_transaction_id" → transactionId làm remark FT
 *   - "loan_currency"       → loại tiền
 *   - request.getDisbursementRequest() → selectedAccountNumber, disbursementType
 *
 * Output vào context:
 *   - "ft_trans_hash" → transHash của FT Bakong (dùng cho DoPushEmoneyLoan TH2)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoDisbursement implements Command {

    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 2000L;

    private static final String TYPE_MBC_ACCOUNT = "MBC_ACCOUNT";
    private static final String TYPE_EMONEY_WALLET = "EMONEY_WALLET";

    private final ApiFundsTransfer apiFundsTransfer;
    private final ApiCustomer apiCustomer;
    private final ComTransRepo comTransRepo;
    private final ComTransDtlLoanDisbursementRepo comTransDtlLoanDisbursementRepo;
    private final ComTransDtlLoanRegistrationRepo registrationRepo;
    private final ComTransProcessRepo comTransProcessRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();

        try {
            DisbursementRequest disbReq = request.getDisbursementRequest();
            String disbursementType = disbReq.getDisbursementType();
            String drawdownAccount = (String) context.get("drawdown_account");
            String loanTxId = (String) context.get("loan_transaction_id");
            String currency = (String) context.get("loan_currency");
            if (Utility.isNull(currency)) currency = disbReq.getCurrency();

            // Tìm ComTrans để audit
            ComTrans comTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(
                    custInfo.getId(), request.getSrvcCd(), Constant.COM_STATUS_INT,
                    request.getTransId(), request.getSessionId());

            if (comTrans == null) {
                log.error("[DoDisbursement] ComTrans not found - requestId:{}", request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            ComTransDtlLoanDisbursement dtl = comTransDtlLoanDisbursementRepo
                    .findById(comTrans.getId()).orElse(null);
            if (dtl == null) {
                log.error("[DoDisbursement] ComTransDtlLoanDisbursement not found - transId:{}", comTrans.getId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            ExecuteT24Output<DataOutput> output;

            if (TYPE_MBC_ACCOUNT.equalsIgnoreCase(disbursementType)) {
                // ========================
                // TH1: INHOUSE Transfer
                // Working Account → TK thanh toán MBC
                // ========================
                log.info("[DoDisbursement] TH1 INHOUSE - requestId:{}", request.getRequestId());
                output = executeInhouseTransfer(comTrans, dtl, drawdownAccount,
                        disbReq.getSelectedAccountNumber(), disbReq.getSelectedAccountName(),
                        disbReq.getDisburseAmount(), currency, loanTxId,
                        custInfo.getId(), request.getRequestId());

            } else if (TYPE_EMONEY_WALLET.equalsIgnoreCase(disbursementType)) {
                // ========================
                // TH2: eMoney Wallet — 2 bước chuyển tiền
                // Bước 2.1: Working Account → Current Account MBC (INHOUSE)
                // Bước 2.2: Current Account MBC → Ví eMoney (CIFTP + retry)
                // ========================
                log.info("[DoDisbursement] TH2 eMoney — bắt đầu 2-step transfer - requestId:{}", request.getRequestId());

                // Tìm tài khoản MBC Current Account của KH (cùng currency với khoản vay)
                AccountBase mbcAccount = getCustomerMbcAccount(custInfo, currency, request.getRequestId());
                if (mbcAccount == null) {
                    log.error("[DoDisbursement] TH2 — Không tìm thấy MBC Current Account (currency:{}) cho KH:{} - requestId:{}",
                            currency, custInfo.getHostCifId(), request.getRequestId());
                    markFailed(comTrans, dtl, null, request.getRequestId());
                    result = new SimpleResult("Không tìm thấy tài khoản thanh toán MBC phù hợp.", false,
                            ResponseCode.TRANSACTION_FAIL.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                String mbcAcctNo = mbcAccount.getAcctId();
                String mbcAcctName = mbcAccount.getAcctnName();
                log.info("[DoDisbursement] TH2 — MBC Current Account: {} ({}), requestId:{}",
                        mbcAcctNo, mbcAcctName, request.getRequestId());

                // ── Bước 2.1: INHOUSE — Working Account → Current Account MBC ──
                log.info("[DoDisbursement] TH2 Bước 2.1 INHOUSE: {} → {} - requestId:{}",
                        drawdownAccount, mbcAcctNo, request.getRequestId());
                ExecuteT24Output<DataOutput> inhouseOutput = executeInhouseTransfer(
                        comTrans, dtl, drawdownAccount,
                        mbcAcctNo, mbcAcctName,
                        disbReq.getDisburseAmount(), currency, loanTxId,
                        custInfo.getId(), request.getRequestId());

                // Kiểm tra kết quả Bước 2.1
                if (Objects.isNull(inhouseOutput)) {
                    log.error("[DoDisbursement] TH2 Bước 2.1 INHOUSE TIMEOUT - requestId:{}", request.getRequestId());
                    markPending(comTrans, dtl, request.getRequestId());
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(inhouseOutput.getStatus())) {
                    String errDesc = inhouseOutput.getErrorInfo() != null
                            ? inhouseOutput.getErrorInfo().getErrorDesc() : "INHOUSE transfer failed";
                    if ("002".equals(inhouseOutput.getSoaErrorCode())) {
                        markPending(comTrans, dtl, request.getRequestId());
                        result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                                ResponseCode.REQUEST_TIMEOUT.getCode());
                    } else {
                        markFailed(comTrans, dtl, inhouseOutput, request.getRequestId());
                        result = new SimpleResult(errDesc, false,
                                inhouseOutput.getErrorInfo() != null
                                        ? inhouseOutput.getErrorInfo().getErrorCode()
                                        : ResponseCode.TRANSACTION_FAIL.getCode());
                    }
                    context.setResult(result);
                    return !result.isOk();
                }

                // Bước 2.1 thành công — tiền đã về Current Account MBC
                String inhouseFt = inhouseOutput.getData().getT24Ft();
                log.info("[DoDisbursement] TH2 Bước 2.1 INHOUSE SUCCESS - ft:{}, requestId:{}", inhouseFt, request.getRequestId());

                // ── Bước 2.2: CIFTP — Current Account MBC → Ví eMoney (retry 3 lần) ──
                log.info("[DoDisbursement] TH2 Bước 2.2 CIFTP: {} → {} - requestId:{}",
                        mbcAcctNo, disbReq.getSelectedAccountNumber(), request.getRequestId());
                output = executeWithRetry(comTrans, dtl,
                        mbcAcctNo, mbcAcctName,                                      // debit = MBC current account
                        disbReq.getSelectedAccountNumber(), disbReq.getSelectedAccountName(), // credit = ví eMoney
                        disbReq.getDisburseAmount(), currency, loanTxId,
                        custInfo.getId(), request.getRequestId());

                // Nếu Bước 2.2 null (timeout sau 3 lần retry) → tiền đã ở MBC account, mark PND
                if (Objects.isNull(output) || (output != null && "002".equals(output.getSoaErrorCode()))) {
                    log.warn("[DoDisbursement] TH2 Bước 2.2 CIFTP TIMEOUT/PND — tiền đã ở MBC account:{} - requestId:{}",
                            mbcAcctNo, request.getRequestId());
                    // Lưu FT của bước 2.1 vào dtl trước khi mark pending
                    dtl.setFt(inhouseFt);
                    markPending(comTrans, dtl, request.getRequestId());
                    result = new SimpleResult(
                            "Your Salary Advance funds are available in your MB Cambodia account. " +
                            "Please transfer the funds to your eMoney wallet manually if needed.",
                            false, ResponseCode.REQUEST_TIMEOUT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                // Nếu Bước 2.2 lỗi nghiệp vụ (không phải timeout)
                if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                    String errDesc = output.getErrorInfo() != null
                            ? output.getErrorInfo().getErrorDesc() : "CIFTP transfer failed";
                    // Tiền đã ở MBC account nhưng CIFTP lỗi → mark PND (không mất tiền)
                    dtl.setFt(inhouseFt);
                    markPending(comTrans, dtl, request.getRequestId());
                    result = new SimpleResult(
                            "Your Salary Advance funds are available in your MB Cambodia account. " +
                            "Please transfer the funds to your eMoney wallet manually if needed.",
                            false, ResponseCode.TRANSACTION_FAIL.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                // Bước 2.2 thành công → tiếp tục xử lý kết quả bên dưới

            } else {
                log.error("[DoDisbursement] Unknown disbursementType:{} - requestId:{}",
                        disbursementType, request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            // Xử lý kết quả
            if (Objects.isNull(output)) {
                // Sau retry vẫn null → PND
                markPending(comTrans, dtl, request.getRequestId());
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                        ResponseCode.REQUEST_TIMEOUT.getCode());

            } else if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                String errDesc = output.getErrorInfo() != null
                        ? output.getErrorInfo().getErrorDesc() : "Transfer failed";
                if ("002".equals(output.getSoaErrorCode())) {
                    markPending(comTrans, dtl, request.getRequestId());
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());
                } else {
                    markFailed(comTrans, dtl, output, request.getRequestId());
                    result = new SimpleResult(errDesc, false,
                            output.getErrorInfo() != null
                                    ? output.getErrorInfo().getErrorCode()
                                    : ResponseCode.TRANSACTION_FAIL.getCode());
                }
            } else {
                // SUCCESS
                String ft = output.getData().getT24Ft();
                String transHash = output.getData() != null ? output.getData().getTransHash() : null;
                markSuccess(comTrans, dtl, output, request.getRequestId());

                // Cập nhật REGISTRATION → SUCCESS
                String registrationId = (String) context.get("registration_id");
                if (!Utility.isNull(registrationId)) {
                    ComTransDtlLoanRegistration reg = registrationRepo.findByIdAndStatus(
                            registrationId, Constant.COM_STATUS_INT);
                    if (reg != null) {
                        reg.setStatus(Constant.STATUS_SUCCESS);
                        reg.setStep("DISBURSEMENT_DONE");
                        registrationRepo.save(reg);
                    }
                }

                // Ghi transHash vào context để DoPushEmoneyLoan (TH2) dùng
                if (!Utility.isNull(transHash)) {
                    context.put("ft_trans_hash", transHash);
                }

                response.setFt(ft);

                // Build DisbursementSuccessData cho Success screen (Figma)
                try {
                    String ldId = (String) context.get("ld_id");
                    // Lấy loanFee & actualLoanAmount từ MS Loan response (đã lưu vào context ở DoCreateLoan)
                    String loanFeeStr = (String) context.get("loan_fee");
                    String actualAmountStr = (String) context.get("actual_loan_amount");

                    // Lấy dueDate từ Registration
                    String registrationId = (String) context.get("registration_id");
                    ComTransDtlLoanRegistration reg = null;
                    if (!Utility.isNull(registrationId)) {
                        reg = registrationRepo.findById(registrationId).orElse(null);
                    }
                    String dueDateStr = null;
                    if (reg != null && reg.getLoanDueDate() != null) {
                        dueDateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(reg.getLoanDueDate());
                    }

                    java.math.BigDecimal feeAmount = !Utility.isNull(loanFeeStr)
                            ? new java.math.BigDecimal(loanFeeStr) : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal disbAmt = disbReq.getDisburseAmount() != null
                            ? new java.math.BigDecimal(disbReq.getDisburseAmount()) : java.math.BigDecimal.ZERO;
                    // actualLoanAmount = loanAmount - loanFee (từ MS Loan)
                    java.math.BigDecimal receivingAmt = !Utility.isNull(actualAmountStr)
                            ? new java.math.BigDecimal(actualAmountStr)
                            : disbAmt.subtract(feeAmount);
                    // Transaction Code: FT (TH1) hoặc transHash Bakong (TH2)
                    String txCode = !Utility.isNull(transHash) ? transHash : ft;

                    com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementSuccessData successData =
                            com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementSuccessData.builder()
                                    .loanId(ldId != null ? ldId : ft)
                                    .disbursementDate(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE))
                                    .dueDate(dueDateStr)
                                    .fee(feeAmount)
                                    .receivingAmount(receivingAmt)
                                    .disbursementAccount(disbReq.getSelectedAccountNumber())
                                    .transactionCode(txCode)
                                    .currency(currency)
                                    .disburseAmount(disbAmt)
                                    .build();
                    response.setDisbursementSuccessData(successData);
                } catch (Exception ex) {
                    log.warn("[DoDisbursement] Build successData failed (non-critical): {}", ex.getMessage());
                }

                RedisServer.removeCacheRedis(ServiceConstant.REDIS_KEY_LIST_LOAN + request.getSessionId());

                log.info("[DoDisbursement] SUCCESS - ft:{}, requestId:{}", ft, request.getRequestId());
            }

        } catch (Exception e) {
            log.error("[DoDisbursement] Exception - requestId:{}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        context.setResponse(response);
        return !result.isOk();
    }

    // ─────────────────────────────────────────────────────────────────
    // TH1: INHOUSE Transfer
    // ─────────────────────────────────────────────────────────────────
    private ExecuteT24Output<DataOutput> executeInhouseTransfer(
            ComTrans comTrans, ComTransDtlLoanDisbursement dtl,
            String debitAcctNo, String creditAcctNo, String creditAcctName,
            String amount, String currency, String remark,
            String custId, String requestId) throws Exception {

        FundsTransferInfo info = buildTransferInfo(
                debitAcctNo, "drawdownAccount", currency,    // debit = working account
                creditAcctNo, creditAcctName, currency,      // credit = TK thanh toán KH
                amount, currency,
                dtl.getBranchCode(), remark,
                "INHOUSE"           // transferType
        );

        return apiFundsTransfer.makeTransfer(info, custId, requestId, comTrans.getId());
    }

    // ─────────────────────────────────────────────────────────────────
    // TH2 Bước 2.2: CIFTP Transfer với Retry tối đa 3 lần khi timeout
    // Debit = Current Account MBC, Credit = Ví eMoney
    // ─────────────────────────────────────────────────────────────────
    private ExecuteT24Output<DataOutput> executeWithRetry(
            ComTrans comTrans, ComTransDtlLoanDisbursement dtl,
            String debitAcctNo, String debitAcctName,
            String creditWalletNo, String creditWalletName,
            String amount, String currency, String remark,
            String custId, String requestId) throws Exception {

        FundsTransferInfo info = buildTransferInfo(
                debitAcctNo, debitAcctName, currency,        // debit = MBC current account
                creditWalletNo, creditWalletName, currency,  // credit = ví eMoney
                amount, currency,
                dtl.getBranchCode(), remark,
                "CIFTP"             // transferType
        );

        ExecuteT24Output<DataOutput> output = null;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            log.info("[DoDisbursement] TH2 Bước 2.2 CIFTP attempt {}/{} - requestId:{}", attempt, MAX_RETRY, requestId);
            output = apiFundsTransfer.makeTransfer(info, custId, requestId, comTrans.getId());

            if (output != null) {
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                    log.info("[DoDisbursement] TH2 CIFTP SUCCESS on attempt {} - requestId:{}", attempt, requestId);
                    return output;
                }
                // Lỗi timeout (002) → retry
                if ("002".equals(output.getSoaErrorCode())) {
                    log.warn("[DoDisbursement] CIFTP timeout (002) attempt {}/{} - requestId:{}", attempt, MAX_RETRY, requestId);
                    if (attempt < MAX_RETRY) {
                        Thread.sleep(RETRY_DELAY_MS);
                    }
                    continue;
                }
                // Lỗi nghiệp vụ khác → không retry, trả về luôn
                log.warn("[DoDisbursement] CIFTP business error (non-timeout): soaErrorCode={}, requestId:{}",
                        output.getSoaErrorCode(), requestId);
                return output;
            }
            // null = timeout → retry
            if (attempt < MAX_RETRY) {
                log.warn("[DoDisbursement] CIFTP null-timeout attempt {}, retrying in {}ms...", attempt, RETRY_DELAY_MS);
                Thread.sleep(RETRY_DELAY_MS);
            }
        }
        return output;
    }

    // ─────────────────────────────────────────────────────────────────
    // Builder helper
    // ─────────────────────────────────────────────────────────────────
    private FundsTransferInfo buildTransferInfo(
            String debitAcctNo, String debitAcctName, String debitCcy,
            String creditAcctNo, String creditAcctName, String creditCcy,
            String amount, String amountCcy,
            String branchCode, String remark,
            String transferType) {

        FundsTransferInfo info = new FundsTransferInfo();

        DebitAccount debit = new DebitAccount();
        debit.setAccountNumber(debitAcctNo);
        debit.setAccountName(debitAcctName);
        debit.setAccountCurrency(debitCcy);
        debit.setAccountType("ACCOUNT");
        info.setDebitAccount(debit);

        CreditAccount credit = new CreditAccount();
        credit.setAccountNumber(creditAcctNo);
        credit.setAccountName(creditAcctName);
        credit.setAccountCurrency(creditCcy);
        credit.setAccountType("ACCOUNT");
        info.setCreditAccount(credit);

        Amount amt = new Amount();
        amt.setAmount(new BigDecimal(amount));
        amt.setCurrency(amountCcy);
        // TH2 (CIFTP/Bakong): bắt buộc truyền benAmount + benCurrency
        if ("CIFTP".equalsIgnoreCase(transferType)) {
            amt.setBenAmount(new BigDecimal(amount));
            amt.setBenCurrency(amountCcy);
        }
        info.setAmount(amt);

        info.setCreditBank(new CreditBank());
        info.setBranchCode(branchCode);
        info.setRemark(remark);
        info.setTransferType(transferType);
        info.setTransactionType("AC"); // BRS 6.5.8: transactionType = "AC"

        // BRS: TH1 action="INHOUSE", TH2 không truyền (mặc định AUTO_AUTH_NO_HOLD)
        if ("INHOUSE".equalsIgnoreCase(transferType)) {
            info.setAction("INHOUSE");
        }

        AddInfoList addInfo = new AddInfoList();
        addInfo.setName("ORDERING_BANK");
        addInfo.setValue("MBC");
        List<AddInfoList> addInfoList = new ArrayList<>();
        addInfoList.add(addInfo);
        info.setAddInfoList(addInfoList);

        return info;
    }

    // ─────────────────────────────────────────────────────────────────
    // Helper: Tìm MBC Current Account của KH theo currency
    // ─────────────────────────────────────────────────────────────────
    private AccountBase getCustomerMbcAccount(CustInfo custInfo, String currency, String requestId) {
        try {
            NonSavingAcctInput input = new NonSavingAcctInput();
            input.setCustomerId(custInfo.getHostCifId());
            ExecuteT24Output<List<AccountBase>> ilResp = apiCustomer
                    .getNonSavingAccountListOtherSalary(input, custInfo.getId(), requestId);
            if (ilResp != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(ilResp.getStatus())
                    && ilResp.getData() != null) {
                for (AccountBase acct : ilResp.getData()) {
                    if (!currency.equalsIgnoreCase(acct.getAcctnCurrency())) continue;
                    if (!Constant.ACCT_STATUS_ACTIVE.equalsIgnoreCase(acct.getAcctnStatus())) continue;
                    // Bỏ qua tài khoản bị hạn chế giao dịch
                    List<PostingRestrict> restricts = acct.getPostingRestrictList();
                    if (restricts != null && restricts.stream()
                            .anyMatch(r -> r != null && r.getId() != null && !r.getId().trim().isEmpty())) continue;
                    // Chỉ lấy tài khoản thường (non-EMONEY)
                    if ("EMONEY".equalsIgnoreCase(acct.getParticipantCode())) continue;
                    return acct;
                }
            }
        } catch (Exception e) {
            log.error("[DoDisbursement] Error fetching customer MBC account - custId:{}, requestId:{}",
                    custInfo.getId(), requestId, e);
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────
    // DB update helpers
    // ─────────────────────────────────────────────────────────────────
    private void markSuccess(ComTrans comTrans, ComTransDtlLoanDisbursement dtl,
                             ExecuteT24Output<DataOutput> output, String requestId) {
        String ft = output.getData().getT24Ft();
        comTrans.setFt(ft);
        comTrans.setStatus(Constant.COM_STATUS_COM);
        comTransRepo.saveAndFlush(comTrans);
        dtl.setFt(ft);
        dtl.setStatus(Constant.COM_STATUS_COM);
        dtl.setRequestId(requestId);
        comTransDtlLoanDisbursementRepo.saveAndFlush(dtl);
        saveProcess(comTrans, output, Constant.COM_STATUS_COM);
    }

    private void markFailed(ComTrans comTrans, ComTransDtlLoanDisbursement dtl,
                            ExecuteT24Output<DataOutput> output, String requestId) {
        comTrans.setStatus(Constant.COM_STATUS_FAIL);
        comTransRepo.saveAndFlush(comTrans);
        dtl.setStatus(Constant.COM_STATUS_FAIL);
        dtl.setRequestId(requestId);
        comTransDtlLoanDisbursementRepo.saveAndFlush(dtl);
        saveProcess(comTrans, output, Constant.COM_STATUS_FAIL);
    }

    private void markPending(ComTrans comTrans, ComTransDtlLoanDisbursement dtl, String requestId) {
        comTrans.setStatus(Constant.COM_STATUS_PND);
        comTransRepo.saveAndFlush(comTrans);
        dtl.setStatus(Constant.COM_STATUS_PND);
        dtl.setRequestId(requestId);
        comTransDtlLoanDisbursementRepo.saveAndFlush(dtl);
        com.mbc.common.entity.ComTransProcess p = new com.mbc.common.entity.ComTransProcess();
        p.setSrvcCd(comTrans.getSrvcCd());
        p.setTransId(comTrans.getId());
        p.setStatus(Constant.COM_STATUS_PND);
        comTransProcessRepo.saveAndFlush(p);
    }

    private void saveProcess(ComTrans comTrans, ExecuteT24Output<DataOutput> output, String status) {
        com.mbc.common.entity.ComTransProcess p = new com.mbc.common.entity.ComTransProcess();
        p.setSrvcCd(comTrans.getSrvcCd());
        p.setTransId(comTrans.getId());
        p.setStatus(status);
        if (output != null && Constant.COM_STATUS_COM.equals(status)) {
            p.setFt(output.getData().getT24Ft());
        }
        if (output != null && output.getErrorInfo() != null) {
            p.setErrorCode(output.getErrorInfo().getErrorCode());
            p.setErrorDesc(output.getErrorInfo().getErrorDesc());
        }
        comTransProcessRepo.saveAndFlush(p);
    }
}
