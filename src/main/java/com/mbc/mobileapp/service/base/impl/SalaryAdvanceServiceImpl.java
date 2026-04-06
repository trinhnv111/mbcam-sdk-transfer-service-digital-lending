package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.object.CustInfo;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.salaryadvance.SaGetLimitInfoResponse;
import com.mbc.mobileapp.service.base.SalaryAdvanceService;
import com.mbc.mobileapp.service.salaryadvance.SaGetCreditLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryAdvanceServiceImpl extends ServiceBase implements SalaryAdvanceService {

    private final SaGetCreditLimitService saGetCreditLimitService;

    @Override
    public SaGetLimitInfoResponse getLimitInfo(CommonServiceRequest request, CustInfo cust) {

        ProcessContext context = loadContext(request, cust);
        SaGetLimitInfoResponse response = new SaGetLimitInfoResponse();
        try {
            saGetCreditLimitService.execute(context);
            logService.execute(context);

            Validator.Result result = context.getResult();
            response.setResult(result);
            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) context.getResponse();
                var limit = res.getSaCreditLimit();
                response.setLimitCode(limit.getLimitCode());
                response.setProductType(limit.getProductType());
                response.setLimitAmount(limit.getLimitAmount());
                response.setUsedAmount(limit.getUsedAmount());
                response.setAvailableAmount(limit.getAvailableAmount());
                response.setCurrency(limit.getCurrency());
                response.setFixedFee(limit.getFixedFee());
                response.setLimitStatus(limit.getLimitStatus());
                response.setEffectiveDate(limit.getEffectiveDate());
                response.setExpiryDate(limit.getExpiryDate());
                // Đọc thêm data phụ từ context.putVar
                response.setHasActiveLoan((Boolean) context.getVar("SA_HAS_ACTIVE_LOAN"));
                response.setActiveLoanCode((String) context.getVar("SA_ACTIVE_LOAN_CODE"));
            }
        } catch (Exception e) {
            log.error("[SA getLimitInfo] error: {}", e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;

    }
}
