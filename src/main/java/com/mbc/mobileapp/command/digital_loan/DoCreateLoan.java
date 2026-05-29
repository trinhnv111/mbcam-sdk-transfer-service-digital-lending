package com.mbc.mobileapp.command.digital_loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLoanDisbursement;
import com.mbc.common.entity.ComTransDtlLoanRegistration;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLoanDisbursementRepo;
import com.mbc.common.repository.ComTransDtlLoanRegistrationRepo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.services.il.loanorigination.DoGenT24DayNowOutput;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.api.ApiMsLoan;
import com.mbc.mobileapp.api.model.digitalloan.input.MsLoanCreateRequest;
import com.mbc.mobileapp.api.model.digitalloan.input.MsLoanCreateRequest.LoanInfo;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DoCreateLoan implements Command {

    private final ApiMsLoan apiMsLoan;
    private final ApiCustomer apiCustomer;
    private final ComTransDtlLoanRegistrationRepo registrationRepo;
    private final ComTransDtlLoanDisbursementRepo disbursementRepo;
    private final ComTransDtlLmtRepository lmtRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean execute(Context ctx) {

        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo custInfo = context.getCustomer();

        try {

            
            // VALIDATE INPUT
            
            DisbursementRequest disbReq = request.getDisbursementRequest();
            if (disbReq == null) {
                log.error("[DoCreateLoan] disbursementRequest null");

                context.setResult(new SimpleResult(
                        ResponseCode.TRANSACTION_FAIL.getDesc(),
                        false,
                        ResponseCode.TRANSACTION_FAIL.getCode()
                ));
                return true;
            }

            String registrationId = disbReq.getTransId();
            if (Utility.isNull(registrationId)) {
                log.error("[DoCreateLoan] registrationId missing");

                context.setResult(new SimpleResult(
                        ResponseCode.TRANSACTION_FAIL.getDesc(),
                        false,
                        ResponseCode.TRANSACTION_FAIL.getCode()
                ));
                return true;
            }

            
            // LOAD DATA
            
            ComTransDtlLoanRegistration registration =
                    registrationRepo.findByIdAndStatus(registrationId, Constant.COM_STATUS_INT);

            if (registration == null) {
                log.error("[DoCreateLoan] registration not found {}", registrationId);

                context.setResult(new SimpleResult(
                        ResponseCode.TRANSACTION_FAIL.getDesc(),
                        false,
                        ResponseCode.TRANSACTION_FAIL.getCode()
                ));
                return true;
            }

            ComTransDtlLmt lmt =
                    lmtRepository.findTopByHostCifIdAndLoanTypeAndStatusOrderByCreatedAtDesc(
                            custInfo.getHostCifId(),
                            SalaryAdvanceConstant.LOAN_TYPE_SALARY_ADVANCE,
                            Constant.STATUS_SUCCESS
                    );

            if (lmt == null) {
                log.error("[DoCreateLoan] LMT not found {}", custInfo.getHostCifId());

                context.setResult(new SimpleResult(
                        ResponseCode.TRANSACTION_FAIL.getDesc(),
                        false,
                        ResponseCode.TRANSACTION_FAIL.getCode()
                ));
                return true;
            }

            
            // GET T24 DATE
            
            ExecuteT24Output<DoGenT24DayNowOutput> t24Res =
                    apiCustomer.genT24DayNow(custInfo.getId(), request.getRequestId());

            if (t24Res == null
                    || !Constant.CALL_MICROSERVICE_SUCCESS.equals(t24Res.getStatus())
                    || t24Res.getData() == null
                    || Utility.isNull(t24Res.getData().getToday())) {

                log.error("[DoCreateLoan] genT24DayNow failed");

                context.setResult(new SimpleResult(
                        ResponseCode.REQUEST_TIMEOUT.getDesc(),
                        false,
                        ResponseCode.REQUEST_TIMEOUT.getCode()
                ));
                return true;
            }

            String t24Day = t24Res.getData().getToday();

            
            // COMPUTE DATA
            
            SimpleDateFormat sdfApi = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

            String valueDateStr = t24Day;
            String maturityDateStr = lmt.getEndDate() != null
                    ? sdfApi.format(lmt.getEndDate())
                    : t24Day;

            String employmentDateStr = lmt.getEmploymentStartDate() != null
                    ? sdfDisplay.format(lmt.getEmploymentStartDate())
                    : null;

            String loanCurrency = disbReq.getCurrency() != null
                    ? disbReq.getCurrency()
                    : registration.getAccountCurrency();

            Map<String, Object> salaryMap =
                    objectMapper.readValue(lmt.getSalaryInfoDetail(), Map.class);

            BigDecimal salaryT1USD =
                    new BigDecimal(String.valueOf(salaryMap.get("salaryAmountT1USD")));

            
            // BUILD LOAN INFO (NESTED)
            
            LoanInfo loanInfo = new LoanInfo();
            loanInfo.setLoanAmount(registration.getLoanAmount());
            loanInfo.setLoanCurrency(loanCurrency);
            loanInfo.setLoanInterest(BigDecimal.ZERO);
            loanInfo.setLoanInterestSpread(null);
            loanInfo.setValueDate(valueDateStr);
            loanInfo.setMaturityDate(maturityDateStr);
            loanInfo.setChannel(SalaryAdvanceConstant.CHANNEL);
            loanInfo.setProduct(SalaryAdvanceConstant.PRODUCT);
            loanInfo.setSubProduct(SalaryAdvanceConstant.SUB_PRODUCT);
            loanInfo.setPartnerCode(SalaryAdvanceConstant.PARTNER_CODE);

            MsLoanCreateRequest msRequest = MsLoanCreateRequest.builder()
                    .customerCode(lmt.getHostCifId())
                    .customerName(lmt.getFullName())
                    .phoneNumber(lmt.getPhoneNumber())
                    .occupation(lmt.getOccupation())
                    .employmentDate(employmentDateStr)
                    .monthlySalary(salaryT1USD)
                    .selfEmployment("N")
                    .salaryCurrency(lmt.getCurrency())
                    .loanInfo(loanInfo)
                    .build();

            
            // CALL MS LOAN
            
            ExecuteT24Output<MsLoanCreateOutput> output =
                    apiMsLoan.createLoan(msRequest, custInfo.getId(), request.getRequestId());

            if (output == null) {
                context.setResult(new SimpleResult(
                        ResponseCode.REQUEST_TIMEOUT.getDesc(),
                        false,
                        ResponseCode.REQUEST_TIMEOUT.getCode()
                ));
                return true;
            }

            if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {

                String msg = output.getErrorInfo() != null
                        ? output.getErrorInfo().getErrorDesc()
                        : "createLoan failed";

                context.setResult(new SimpleResult(
                        msg,
                        false,
                        output.getErrorInfo() != null
                                ? output.getErrorInfo().getErrorCode()
                                : ResponseCode.TRANSACTION_FAIL.getCode()
                ));
                return true;
            }

            MsLoanCreateOutput out = output.getData();

            if (out == null || Utility.isNull(out.getLdId())) {
                context.setResult(new SimpleResult(
                        ResponseCode.TRANSACTION_FAIL.getDesc(),
                        false,
                        ResponseCode.TRANSACTION_FAIL.getCode()
                ));
                return true;
            }

            
            // CONTEXT OUTPUT
            
            context.put("ms_loan_response", out);

            
            // UPDATE REGISTRATION
            
            registration.setLdId(out.getLdId());
            registration.setLimitId(out.getLimitId());
            registration.setCreditContractId(out.getCreditContractId());
            registration.setStep("CREATE_LOAN");

            if (!Utility.isNull(out.getLoanFee())) {
                registration.setCbcFee(new BigDecimal(out.getLoanFee()));
            }

            registrationRepo.saveAndFlush(registration);

            
            // SAVE DISBURSEMENT
            
            String currency = out.getDrawdownAccountCurrency() != null
                    ? out.getDrawdownAccountCurrency()
                    : loanCurrency;

            BigDecimal amount = registration.getLoanAmount() != null
                    ? registration.getLoanAmount()
                    : BigDecimal.ZERO;

            ComTransDtlLoanDisbursement dtl =
                    ComTransDtlLoanDisbursement.builder()
                            .id(registrationId)
                            .custId(custInfo.getId())
                            .status(Constant.COM_STATUS_INT)
                            .debitAcctNo(out.getDrawdownAccount())
                            .debitAcctName(out.getDrawdownAccountName())
                            .debitAcctCcy(currency)
                            .debitAmount(amount)
                            .crebitAcctNo(disbReq.getSelectedAccountNumber())
                            .crebitAcctName(disbReq.getSelectedAccountName())
                            .crebitAcctCcy(currency)
                            .crebitAmount(amount)
                            .amount(amount)
                            .currency(currency)
                            .transferType(disbReq.getDisbursementType())
                            .productType(SalaryAdvanceConstant.PRODUCT)
                            .transactionDate(new Date())
                            .build();

            disbursementRepo.saveAndFlush(dtl);

            log.info("[DoCreateLoan] SUCCESS ldId={}", out.getLdId());

        } catch (Exception e) {
            log.error("[DoCreateLoan] exception", e);

            context.setResult(new SimpleResult(
                    ResponseCode.TRANSACTION_FAIL.getDesc(),
                    false,
                    ResponseCode.TRANSACTION_FAIL.getCode()
            ));
            return true;
        }

        context.setResult(result);
        return false;
    }
}