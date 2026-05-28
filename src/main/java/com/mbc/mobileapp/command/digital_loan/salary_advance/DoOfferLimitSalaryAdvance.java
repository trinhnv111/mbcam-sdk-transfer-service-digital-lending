package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.ApiMsLoan;
import com.mbc.mobileapp.api.model.salary_advance.input.MsLoanOfferLimitRequest;
import com.mbc.mobileapp.api.model.salary_advance.output.EmSalaryInfo;
import com.mbc.mobileapp.api.model.salary_advance.output.MsLoanOfferLimitResponse;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.constant.SalaryAdvanceConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Command tính toán hạn mức Salary Advance.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoOfferLimitSalaryAdvance implements Command {

    private final ApiMsLoan apiMsLoan;
    private final ComTransDtlLmtRepository comTransDtlLmtRepo;
    private final ObjectMapper objectMapper;

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo custInfo = context.getCustomer();

        try {
            String transId = (String) context.get("transId");
            if (Utility.isNull(transId) && request.getSalaryAdvanceCreateRequest() != null) {
                transId = request.getSalaryAdvanceCreateRequest().getTransId();
            }
            String hostCifId = custInfo.getHostCifId();

            AppLog.info("[SA CALCULATE LIMIT] Start - requestId: " + request.getRequestId()
                    + ", transId: " + transId + ", hostCifId: " + hostCifId);

            // Lấy salaryInfo đã lưu trong ComTransDtlLmt (salaryInfoDetail JSON)
            EmSalaryInfo emSalaryInfo = null;
            String currency = "USD"; // Default fallback
            if (!Utility.isNull(transId)) {
                ComTransDtlLmt record = comTransDtlLmtRepo.findById(transId).orElse(null);
                if (record != null) {
                    if (!Utility.isNull(record.getCurrency())) {
                        currency = record.getCurrency();
                    }
                    if (!Utility.isNull(record.getSalaryInfoDetail())) {
                        emSalaryInfo = objectMapper.readValue(record.getSalaryInfoDetail(), EmSalaryInfo.class);
                    }
                }
            }

            // Build request body cho MS Loan
            MsLoanOfferLimitRequest msRequest = buildMsLoanRequest(hostCifId, emSalaryInfo, currency);

            // Call MS Loan offer-limit
            MsLoanOfferLimitResponse msResponse = apiMsLoan.offerLimit(
                    msRequest, custInfo.getId(), request.getRequestId());

            if (msResponse == null || msResponse.getData() == null) {
                log.error("[SA OFFER LIMIT] MS Loan response is null - requestId: {}", request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            MsLoanOfferLimitResponse.LimitData limitData = msResponse.getData();

            // Đẩy kết quả vào context cho DoUpdateSalaryAdvanceLimit
            context.put("sa_limit", limitData.getLimitAmount());
            context.put("sa_currency", limitData.getLimitCurrency());
            context.put("sa_limit_value_date", limitData.getLimitValueDate());
            context.put("sa_limit_end_date", limitData.getLimitEndDate());

            AppLog.info("[SA CALCULATE LIMIT] Done - limitAmount: " + limitData.getLimitAmount()
                    + " " + limitData.getLimitCurrency());

        } catch (Exception e) {
            log.error("[SA CALCULATE LIMIT] Exception - requestId: {}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }

    /**
     * Build MS Loan request body từ hostCifId và EmSalaryInfo.
     * salaryInfo chứa T1/T2/T3 lương.
     */
    private MsLoanOfferLimitRequest buildMsLoanRequest(String hostCifId, EmSalaryInfo salary, String currency) {

        List<MsLoanOfferLimitRequest.SalaryDetail> salaryDetails = new ArrayList<>();

        if (salary != null) {

//            BigDecimal amount = null;
//
//            // T3 (latest)
//            amount = pickSalary(salary.getSalaryAmountT3USD(), salary.getSalaryAmountT3KHR());
//
//            // fallback T2
//            if (amount == null) {
//                amount = pickSalary(salary.getSalaryAmountT2USD(), salary.getSalaryAmountT2KHR());
//            }
//
//            // fallback T1
//            if (amount == null) {
//                amount = pickSalary(salary.getSalaryAmountT1USD(), salary.getSalaryAmountT1KHR());
//            }
//
//            if (amount != null) {
//                addSalaryDetail(salaryDetails, amount);
//            }
            // Tháng 1 (T1)
            addSalaryDetail(salaryDetails, salary.getSalaryAmountT1USD() != null ? salary.getSalaryAmountT1USD() : salary.getSalaryAmountT1KHR()
            );

            // Tháng 2 (T2)
            addSalaryDetail(salaryDetails, salary.getSalaryAmountT2USD() != null ? salary.getSalaryAmountT2USD() : salary.getSalaryAmountT2KHR()
            );
            // Tháng 3 (T3)
            addSalaryDetail(salaryDetails, salary.getSalaryAmountT3USD() != null ? salary.getSalaryAmountT3USD() : salary.getSalaryAmountT3KHR()
            );


        }

        return MsLoanOfferLimitRequest.builder()
                .customerCode(hostCifId)
                .channel(SalaryAdvanceConstant.CHANNEL)
                .product(SalaryAdvanceConstant.PRODUCT)
                .subProduct(SalaryAdvanceConstant.SUB_PRODUCT)
                .partnerCode(SalaryAdvanceConstant.PARTNER_CODE)
                .limitCurrency(currency)
                .salaryDetail(salaryDetails)
                .build();
    }

//    /**
//     * USD priority, fallback KHR
//     */
//    private BigDecimal pickSalary(BigDecimal usd, BigDecimal khr) {
//        return usd != null ? usd : khr;
//    }

    private void addSalaryDetail(List<MsLoanOfferLimitRequest.SalaryDetail> details, BigDecimal amount) {
        if (amount != null) {
            details.add(MsLoanOfferLimitRequest.SalaryDetail.builder()
                    .salaryAmount(amount)
                    .build());
        }
    }
}
