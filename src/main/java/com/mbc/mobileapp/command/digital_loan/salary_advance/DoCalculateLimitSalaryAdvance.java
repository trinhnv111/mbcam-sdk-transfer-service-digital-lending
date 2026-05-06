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
import com.mbc.mobileapp.api.model.salary_advance.input.MsLoanCalculateLimitRequest;
import com.mbc.mobileapp.api.model.salary_advance.output.EmSalaryInfo;
import com.mbc.mobileapp.api.model.salary_advance.output.MsLoanCalculateLimitResponse;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command tính toán hạn mức Salary Advance.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoCalculateLimitSalaryAdvance implements Command {

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
            if (!Utility.isNull(transId)) {
                ComTransDtlLmt record = comTransDtlLmtRepo.findById(transId).orElse(null);
                if (record != null && !Utility.isNull(record.getSalaryInfoDetail())) {
                    emSalaryInfo = objectMapper.readValue(record.getSalaryInfoDetail(), EmSalaryInfo.class);
                }
            }

            // Build request body cho MS Loan
            MsLoanCalculateLimitRequest msRequest = buildMsLoanRequest(hostCifId, emSalaryInfo);

            // Call MS Loan
            MsLoanCalculateLimitResponse msResponse = apiMsLoan.calculateLimit(
                    msRequest, custInfo.getId(), request.getRequestId());

            if (msResponse == null || msResponse.getData() == null) {
                log.error("[SA CALCULATE LIMIT] MS Loan response is null - requestId: {}", request.getRequestId());
                result = new SimpleResult("Failed to calculate limit", false, ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            MsLoanCalculateLimitResponse.LimitData limitData = msResponse.getData();

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
     * salaryInfo chứa T1/T2/T3 lương USD+KHR → map thành 3 SalaryMonth.
     */
    private MsLoanCalculateLimitRequest buildMsLoanRequest(String hostCifId, EmSalaryInfo salary) {
        List<MsLoanCalculateLimitRequest.SalaryMonth> salaryMonths = new ArrayList<>();

        if (salary != null) {
            // Tháng 1 (T1)
            salaryMonths.add(buildSalaryMonth(salary.getSalaryAmountT1USD(), salary.getSalaryAmountT1KHR()));
            // Tháng 2 (T2)
            salaryMonths.add(buildSalaryMonth(salary.getSalaryAmountT2USD(), salary.getSalaryAmountT2KHR()));
            // Tháng 3 (T3)
            salaryMonths.add(buildSalaryMonth(salary.getSalaryAmountT3USD(), salary.getSalaryAmountT3KHR()));
        }

        return MsLoanCalculateLimitRequest.builder()
                .customerCode(hostCifId)
                .channel("SDK")
                .product("DIGITAL_LOAN")
                .subProduct("SALARY_ADVANCE")
                .partnerCode("EMONEY")
                .limitCurrency("USD")
                .salary(salaryMonths)
                .build();
    }

    /**
     *  SalaryMonth chứa detail USD + KHR
     */
    private MsLoanCalculateLimitRequest.SalaryMonth buildSalaryMonth(BigDecimal usd, BigDecimal khr) {
        List<MsLoanCalculateLimitRequest.SalaryDetail> details = new ArrayList<>();

        if (usd != null) {
            details.add(MsLoanCalculateLimitRequest.SalaryDetail.builder()
                    .salaryAmount(usd)
                    .salaryCurrency("USD")
                    .build());
        }
        if (khr != null) {
            details.add(MsLoanCalculateLimitRequest.SalaryDetail.builder()
                    .salaryAmount(khr)
                    .salaryCurrency("KHR")
                    .build());
        }

        return MsLoanCalculateLimitRequest.SalaryMonth.builder()
                .salaryDetail(details)
                .build();
    }
}
