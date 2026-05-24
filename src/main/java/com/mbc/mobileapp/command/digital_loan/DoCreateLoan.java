package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLoanDisbursement;
import com.mbc.common.entity.ComTransDtlLoanRegistration;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLoanDisbursementRepo;
import com.mbc.common.repository.ComTransDtlLoanRegistrationRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.ApiMsLoan;
import com.mbc.mobileapp.api.model.digitalloan.input.MsLoanCreateRequest;
import com.mbc.mobileapp.api.model.digitalloan.output.MsLoanCreateOutput;
import com.mbc.mobileapp.constant.SalaryAdvanceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Step: Hạch toán Core — Gọi MS Loan createLoan → T24 mở LD account
 *
 * Input từ context:
 *   - request.getDisbursementRequest() → disburseAmount, selectedAccountNumber, disbursementType
 *   - "registration_id" (UUID của ComTransDtlLoanRegistration tạo ở DoGetLoanInfo)
 *
 * Output vào context:
 *   - "ld_id"              → mã khoản vay T24 (dùng cho eMoney push + MakeTransfer)
 *   - "drawdown_account"   → working account nhận giải ngân (dùng cho MakeTransfer TH1)
 *   - "loan_transaction_id"→ transactionId làm remark FT
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoCreateLoan implements Command {

    private final ApiMsLoan apiMsLoan;
    private final ComTransDtlLoanRegistrationRepo registrationRepo;
    private final ComTransDtlLoanDisbursementRepo disbursementRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo custInfo = context.getCustomer();

        try {
            DisbursementRequest disbReq = request.getDisbursementRequest();

            // Lấy registration record để lấy docIdEng, loanAmount, loanDueDate
            String registrationId = (String) context.get("registration_id");
            ComTransDtlLoanRegistration registration = null;
            if (!Utility.isNull(registrationId)) {
                registration = registrationRepo.findByIdAndStatus(registrationId, Constant.COM_STATUS_INT);
            }
            if (registration == null) {
                log.error("[DoCreateLoan] registration not found - registrationId:{}", registrationId);
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            // Tính ngày đáo hạn khoản vay (từ limitEndDate đã lưu trong registration)
            String dueDate = registration.getLoanDueDate() != null
                    ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(registration.getLoanDueDate())
                    : LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

            MsLoanCreateRequest msRequest = MsLoanCreateRequest.builder()
                    .customerCode(custInfo.getHostCifId())
                    .loanAmount(disbReq.getDisburseAmount())
                    .loanCurrency(disbReq.getCurrency() != null ? disbReq.getCurrency() : registration.getAccountCurrency())
                    .loanDueDate(dueDate)
                    .disbursementAccount(disbReq.getSelectedAccountNumber())
                    .docIdEng(registration.getDocIdEng())
                    .channel("SDK")
                    .product("DIGITAL_LOAN")
                    .subProduct("SALARY_ADVANCE")
                    .partnerCode("EMONEY")
                    .build();

            log.info("[DoCreateLoan] Calling MS Loan createLoan - requestId:{}, customerCode:{}, amount:{}",
                    request.getRequestId(), custInfo.getHostCifId(), disbReq.getDisburseAmount());

            ExecuteT24Output<MsLoanCreateOutput> output = apiMsLoan.createLoan(
                    msRequest, custInfo.getId(), request.getRequestId());

            if (output == null) {
                log.error("[DoCreateLoan] MS Loan createLoan timeout - requestId:{}", request.getRequestId());
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                        ResponseCode.REQUEST_TIMEOUT.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                String errDesc = output.getErrorInfo() != null ? output.getErrorInfo().getErrorDesc() : "createLoan failed";
                log.error("[DoCreateLoan] MS Loan error - requestId:{}, err:{}", request.getRequestId(), errDesc);
                result = new SimpleResult(errDesc, false,
                        output.getErrorInfo() != null ? output.getErrorInfo().getErrorCode()
                                : ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            MsLoanCreateOutput createOut = output.getData();
            if (createOut == null || Utility.isNull(createOut.getLdId())) {
                log.error("[DoCreateLoan] ldId is null in MS Loan response - requestId:{}", request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            // Lưu vào context để các bước sau dùng
            context.put("ld_id", createOut.getLdId());
            context.put("drawdown_account", createOut.getDrawdownAccount());
            context.put("loan_transaction_id", createOut.getTransactionId());
            context.put("loan_currency", createOut.getCurrency());
            context.put("receiving_amount", createOut.getReceivingAmount());

            // Cập nhật ldId vào REGISTRATION record
            registration.setLdId(createOut.getLdId());
            registration.setStep("CREATE_LOAN");
            registrationRepo.save(registration);

            // ── Tạo ComTransDtlLoanDisbursement ──────────────────────────
            // Record này bắt buộc phải tồn tại trước khi DoDisbursement chạy
            // id = registrationId (cùng PK với ComTrans disbursement)
            String currency = createOut.getCurrency() != null
                    ? createOut.getCurrency()
                    : (disbReq.getCurrency() != null ? disbReq.getCurrency() : registration.getAccountCurrency());
            BigDecimal amount = disbReq.getDisburseAmount() != null
                    ? new java.math.BigDecimal(disbReq.getDisburseAmount()) : java.math.BigDecimal.ZERO;

            ComTransDtlLoanDisbursement dtl = ComTransDtlLoanDisbursement.builder()
                    .id(registrationId)                                // PK = registration id
                    .custId(custInfo.getId())
                    .status(Constant.COM_STATUS_INT)
                    .debitAcctNo(createOut.getDrawdownAccount())      // working account (nguồn tiền)
                    .debitAcctCcy(currency)
                    .debitAmount(amount)
                    .crebitAcctNo(disbReq.getSelectedAccountNumber()) // TK đích của KH
                    .crebitAcctName(disbReq.getSelectedAccountName())
                    .crebitAcctCcy(currency)
                    .crebitAmount(amount)
                    .amount(amount)
                    .currency(currency)
                    .transferType(disbReq.getDisbursementType())      // MBC_ACCOUNT | EMONEY_WALLET
                    .productType("SALARY_ADVANCE")
                    .transactionDate(new java.util.Date())
                    .build();
            disbursementRepo.saveAndFlush(dtl);
            log.info("[DoCreateLoan] Saved DtlLoanDisbursement - id:{}", registrationId);


            log.info("[DoCreateLoan] SUCCESS - ldId:{}, drawdownAccount:{}, requestId:{}",
                    createOut.getLdId(), createOut.getDrawdownAccount(), request.getRequestId());

        } catch (Exception e) {
            log.error("[DoCreateLoan] Exception - requestId:{}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
