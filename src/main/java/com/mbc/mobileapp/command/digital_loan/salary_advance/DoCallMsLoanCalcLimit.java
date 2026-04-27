package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.api.ApiLoanService;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 Gọi MS Loan Service để tính hạn mức ứng lương cho khách hàng
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoCallMsLoanCalcLimit implements Command {

    // TODO: Uncomment khi MS Loan API ready
    // private final ApiLoanService apiLoanService;

    private static final String DEFAULT_CURRENCY = "USD";

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = processContext.getCustomer();

        try {
            ComTransDtlLmt tempRecord = (ComTransDtlLmt) processContext.get("tempRecord");

            log.info("[SA CREATE - CALC LIMIT] Start - requestId:{}, hostCifId:{}, monthlySalary:{}",
                    request.getRequestId(), custInfo.getHostCifId(),
                    tempRecord.getMonthlySalaryAmountUsd());


            // TODO: Replace stub with actual MS Loan API call

            // STUB: Tạm tính limit = monthly salary * 3 (placeholder)
            BigDecimal monthlySalary = tempRecord.getMonthlySalaryAmountUsd();
            BigDecimal calculatedLimit = BigDecimal.ZERO;
            if (monthlySalary != null && monthlySalary.signum() > 0) {
                calculatedLimit = monthlySalary.multiply(new BigDecimal("3"));
            }
            String currency = DEFAULT_CURRENCY;

            // Put kết quả vào context
            processContext.put("calculatedLimit", calculatedLimit);
            processContext.put("limitCurrency", currency);

            log.info("[SA CREATE - CALC LIMIT] Done - requestId:{}, limit:{} {}",
                    request.getRequestId(), calculatedLimit, currency);

        } catch (Exception e) {
            log.error("[SA CREATE - CALC LIMIT] Exception - requestId:{}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    ResponseCode.TRANSACTION_FAIL.getDesc());
        }

        processContext.setResult(result);
        return !result.isOk();
    }
}
