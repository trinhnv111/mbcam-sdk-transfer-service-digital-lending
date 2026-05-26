package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.api.models.fundsTransfer.DataOutput;
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
import com.mbc.mobileapp.api.ApiDisbursementTransfer;
import com.mbc.mobileapp.api.model.digitalloan.output.MsLoanCreateOutput;
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

import static com.mbc.mobileapp.constant.SalaryAdvanceConstant.*;

/**
 * DoDisbursement — Orchestrator chuyển tiền giải ngân Salary Advance.
 *
 * <p>Logic gọi MS FT đã tách sang {@link ApiDisbursementTransfer} theo đúng convention gọi API của hệ thống.
 * Command này chỉ làm 5 việc:
 * <ol>
 *   <li>Load giao dịch từ DB</li>
 *   <li>Resolve remark (transactionId từ MS LOAN)</li>
 *   <li>Gọi Chặng 2 INHOUSE → persist trạng thái</li>
 *   <li>Gọi Chặng 3 CIFTP nếu EMONEY_WALLET → ghi context cho DoPushEmoneyLoan</li>
 *   <li>Trả kết quả cho chain</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoDisbursement implements Command {

    // ── Context keys (dùng chung DoDisbursement ↔ DoPushEmoneyLoan ↔ ServiceImpl) ──
    public static final String CTX_FT_TRANS_HASH  = "ft_trans_hash";
    public static final String CTX_CIFTP_FT       = "ciftp_ft";
    public static final String CTX_CIFTP_STATUS   = "ciftp_status";
    public static final String CTX_MS_LOAN_RESP   = "ms_loan_response";
    public static final String CTX_EM_WALLET_NO   = "em_wallet_number";
    public static final String CTX_EM_WALLET_NAME = "em_wallet_name";

    // ── CIFTP status values ──
    public static final String CIFTP_SUCCESS        = "SUCCESS";
    public static final String CIFTP_FAIL           = "FAIL";
    public static final String CIFTP_FAIL_NO_WALLET = "FAIL_NO_WALLET";

    // ── Dependencies ──
    private final ApiDisbursementTransfer apiDisbursementTransfer;
    private final ComTransRepo comTransRepo;
    private final ComTransDtlLoanDisbursementRepo dtlRepo;
    private final ComTransProcessRepo processRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context  = (ProcessContext) cntxt;
        CommonServiceRequest request  = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();
        String requestId = request.getRequestId();

        try {
            // ── 1. Load giao dịch ──
            ComTrans comTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(
                    custInfo.getId(), request.getSrvcCd(), Constant.COM_STATUS_INT,
                    request.getTransId(), request.getSessionId());
            if (comTrans == null) {
                log.error("[DoDisbursement] ComTrans not found - transId:{}", request.getTransId());
                context.setResult(new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode()));
                return true;
            }

            ComTransDtlLoanDisbursement dtl = dtlRepo.findById(comTrans.getId()).orElse(null);
            if (dtl == null) {
                log.error("[DoDisbursement] Dtl not found - id:{}", comTrans.getId());
                context.setResult(new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode()));
                return true;
            }

            // ── 2. Resolve remark = transactionId từ MS LOAN ──
            String remark = comTrans.getId();
            MsLoanCreateOutput out = (MsLoanCreateOutput) context.get(CTX_MS_LOAN_RESP);
            if (out != null && !Utility.isNull(out.getTransactionId())) {
                remark = out.getTransactionId();
            }

            // ── 3. Chặng 2: INHOUSE ──
            ExecuteT24Output<DataOutput> leg2 =
                    apiDisbursementTransfer.makeInhouseTransfer(comTrans, dtl, custInfo, remark, requestId);

            boolean leg2Success = false;
            String leg2Status;
            String leg2Ft = null;
            String leg2ErrCode = null;
            String leg2ErrDesc = null;

            if (leg2 == null) {
                leg2Status = Constant.COM_STATUS_PND;
            } else if (Constant.CALL_MICROSERVICE_SUCCESS.equals(leg2.getStatus())) {
                leg2Ft = leg2.getData().getT24Ft();
                log.info("[DoDisbursement] Chặng 2 OK - ft:{}", leg2Ft);
                leg2Status = Constant.COM_STATUS_COM;
                response.setFt(leg2Ft);
                leg2Success = true;
            } else {
                leg2ErrCode = leg2.getErrorInfo() == null ? "UNKNOWN" : leg2.getErrorInfo().getErrorCode();
                if (leg2.getErrorInfo() == null) {
                    leg2ErrDesc = "Unknown error";
                } else {
                    String desc = leg2.getErrorInfo().getErrorDesc();
                    String detail = leg2.getErrorInfo().getErrorDetail();
                    leg2ErrDesc = !Utility.isNull(detail) ? desc + " - " + detail : desc;
                }
                leg2Status = (leg2.getSoaErrorCode() != null && "002".equals(leg2.getSoaErrorCode())) ? Constant.COM_STATUS_PND : Constant.COM_STATUS_FAIL;
            }

            // Persist Chặng 2 state
            comTrans.setStatus(leg2Status);
            if (!Utility.isNull(leg2Ft)) comTrans.setFt(leg2Ft);
            comTransRepo.saveAndFlush(comTrans);

            dtl.setStatus(leg2Status);
            if (!Utility.isNull(leg2Ft)) dtl.setFt(leg2Ft);
            dtlRepo.saveAndFlush(dtl);

            ComTransProcess leg2Proc = new ComTransProcess();
            leg2Proc.setSrvcCd(comTrans.getSrvcCd());
            leg2Proc.setTransId(comTrans.getId());
            leg2Proc.setStatus(leg2Status);
            if (!Utility.isNull(leg2Ft))      leg2Proc.setFt(leg2Ft);
            if (!Utility.isNull(leg2ErrCode)) leg2Proc.setErrorCode(leg2ErrCode);
            if (!Utility.isNull(leg2ErrDesc)) leg2Proc.setErrorDesc(leg2ErrDesc);
            processRepo.saveAndFlush(leg2Proc);

            if (!leg2Success) {
                if (leg2 == null || (leg2.getSoaErrorCode() != null && "002".equals(leg2.getSoaErrorCode()))) {
                    context.setResult(new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode()));
                } else {
                    context.setResult(new SimpleResult(leg2ErrDesc, false, leg2ErrCode));
                }
                context.setResponse(response);
                return true; // dừng chain
            }

            // ── 4. Xóa Redis cache ──
            try {
                RedisServer.removeCacheRedis(ServiceConstant.REDIS_KEY_LIST_LOAN + request.getSessionId());
            } catch (Exception e) {
                log.warn("[DoDisbursement] Redis clear failed: {}", e.getMessage());
            }

            // ── 5. Chặng 3: CIFTP (nếu EMONEY_WALLET, non-blocking) ──
            DisbursementRequest disbReq = request.getDisbursementRequest();
            String type = disbReq != null ? disbReq.getDisbursementType() : null;

            if (SalaryAdvanceConstant.DISBURSEMENT_TYPE_EMONEY_WALLET.equals(type)) {
                log.info("[DoDisbursement] Chặng 3 CIFTP bắt đầu - requestId:{}", requestId);

                // Resolve ví eMoney
                Object walletNoObj = context.get(CTX_EM_WALLET_NO);
                String walletNo = (walletNoObj instanceof String) ? (String) walletNoObj : null;
                if (Utility.isNull(walletNo) && disbReq != null) {
                    walletNo = disbReq.getSelectedAccountNumber();
                }

                if (Utility.isNull(walletNo)) {
                    log.error("[DoDisbursement] Chặng 3: walletNo null - requestId:{}", requestId);
                    context.put(CTX_CIFTP_STATUS, CIFTP_FAIL_NO_WALLET);
                } else {
                    Object walletNameObj = context.get(CTX_EM_WALLET_NAME);
                    String walletName = (walletNameObj instanceof String) ? (String) walletNameObj : null;
                    if (Utility.isNull(walletName)) {
                        walletName = dtl.getCrebitAcctName();
                    }

                    String amountStr = dtl.getDebitAmount() != null ? dtl.getDebitAmount().toPlainString() : "0";

                    // Gọi CIFTP (đã có retry bên trong)
                    ExecuteT24Output<DataOutput> output = apiDisbursementTransfer.makeCiftpTransferWithRetry(
                            comTrans,
                            dtl.getCrebitAcctNo(), dtl.getCrebitAcctName(), dtl.getCrebitAcctCcy(),
                            walletNo, walletName, dtl.getCrebitAcctCcy(),
                            amountStr, custInfo, remark, requestId);

                    // Đánh giá kết quả → ghi context
                    if (apiDisbursementTransfer.isTransferSuccess(output)) {
                        String ft3       = output.getData().getT24Ft();
                        String transHash = output.getData().getTransHash();
                        log.info("[DoDisbursement] Chặng 3 OK - ft:{}, transHash:{}, requestId:{}", ft3, transHash, requestId);

                        context.put(CTX_FT_TRANS_HASH, transHash);
                        context.put(CTX_CIFTP_FT, ft3);
                        context.put(CTX_CIFTP_STATUS, CIFTP_SUCCESS);

                        ComTransProcess ciftpProc = new ComTransProcess();
                        ciftpProc.setSrvcCd(comTrans.getSrvcCd());
                        ciftpProc.setTransId(comTrans.getId());
                        ciftpProc.setStatus(Constant.COM_STATUS_COM);
                        ciftpProc.setFt("CIFTP_" + ft3);
                        processRepo.saveAndFlush(ciftpProc);
                    } else {
                        String errCode = "UNKNOWN";
                        String errDesc = "CIFTP timeout sau " + SalaryAdvanceConstant.CIFTP_MAX_RETRY + " lần retry";

                        if (output != null) {
                            errCode = output.getErrorInfo() == null ? "UNKNOWN" : output.getErrorInfo().getErrorCode();
                            if (output.getErrorInfo() == null) {
                                errDesc = "Unknown error";
                            } else {
                                String desc = output.getErrorInfo().getErrorDesc();
                                String detail = output.getErrorInfo().getErrorDetail();
                                errDesc = !Utility.isNull(detail) ? desc + " - " + detail : desc;
                            }
                        }

                        log.error("[DoDisbursement] Chặng 3 FAIL - errCode:{}, requestId:{}", errCode, requestId);

                        context.put(CTX_CIFTP_STATUS, CIFTP_FAIL);

                        ComTransProcess ciftpProc = new ComTransProcess();
                        ciftpProc.setSrvcCd(comTrans.getSrvcCd());
                        ciftpProc.setTransId(comTrans.getId());
                        ciftpProc.setStatus(Constant.COM_STATUS_PND);
                        ciftpProc.setErrorCode(errCode);
                        ciftpProc.setErrorDesc(errDesc);
                        processRepo.saveAndFlush(ciftpProc);
                    }
                }
            }

            // ── Chain tiếp → DoPushEmoneyLoan ──
            context.setResult(Validator.Result.OK);
            context.setResponse(response);
            return false;

        } catch (Exception e) {
            log.error("[DoDisbursement] Exception - requestId:{}", requestId, e);
            context.setResult(new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode()));
            return true;
        }
    }
}
