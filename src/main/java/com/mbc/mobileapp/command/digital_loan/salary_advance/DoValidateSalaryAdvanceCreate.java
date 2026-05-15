package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DoValidateSalaryAdvanceCreate implements Command {

    private static final String CAMBODIA_CODE = "CAMBODIA";

    private static final String MSG_REQUEST_NULL                  = "Request null";
    private static final String MSG_MISSING_REQUIRED_FIELDS       = "Missing required fields";
    private static final String MSG_MISSING_CAMBODIA_BIRTH_DETAIL = "Missing Cambodia place of birth details";

    @Override
    public boolean execute(Context ctx) {
        ProcessContext context = (ProcessContext) ctx;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        SalaryAdvanceCreateRequest req =
                request != null ? request.getSalaryAdvanceCreateRequest() : null;

        if (req == null) {
            log.error("[SalaryAdvanceValidate] {}", MSG_REQUEST_NULL);
            context.setResult(new SimpleResult(MSG_REQUEST_NULL, false, ResponseCode.INVALID_INPUT.getCode()));
            return true;
        }

        if (StringUtils.isBlank(req.getTransId())
                || StringUtils.isBlank(req.getMaritalStatus())
                || StringUtils.isBlank(req.getPlaceOfBirth())
                || StringUtils.isBlank(req.getCurrentAddressProvince())
                || StringUtils.isBlank(req.getCurrentAddressDistrict())
                || StringUtils.isBlank(req.getCurrentAddressWard())) {

            log.error("[SalaryAdvanceValidate] {} - transId: {}", MSG_MISSING_REQUIRED_FIELDS, req.getTransId());
            context.setResult(new SimpleResult(MSG_MISSING_REQUIRED_FIELDS, false, ResponseCode.TRANSACTION_FAIL.getCode()));
            return true;
        }

        if (CAMBODIA_CODE.equalsIgnoreCase(StringUtils.trimToEmpty(req.getPlaceOfBirth()))) {
            if (StringUtils.isBlank(req.getPlaceOfBirthProvince())
                    || StringUtils.isBlank(req.getPlaceOfBirthDistrict())
                    || StringUtils.isBlank(req.getPlaceOfBirthWard())) {

                log.error("[SalaryAdvanceValidate] {} - transId: {}", MSG_MISSING_CAMBODIA_BIRTH_DETAIL, req.getTransId());
                context.setResult(new SimpleResult(MSG_MISSING_CAMBODIA_BIRTH_DETAIL, false, ResponseCode.INVALID_INPUT.getCode()));
                return true;
            }
        }

        context.setResult(Validator.Result.OK);
        return false;
    }
}