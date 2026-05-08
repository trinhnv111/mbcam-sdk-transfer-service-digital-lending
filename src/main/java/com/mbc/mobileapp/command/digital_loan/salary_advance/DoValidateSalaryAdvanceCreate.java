package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DoValidateSalaryAdvanceCreate implements Command {

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        SalaryAdvanceCreateRequest req = request.getSalaryAdvanceCreateRequest();

        if (req == null) {
            log.error("[DoValidateSalaryAdvanceCreate] Request body is empty");
            result = new SimpleResult(
                    ResponseCode.INVALID_INPUT.getDesc(),
                    false,
                    ResponseCode.INVALID_INPUT.getCode()
            );
            context.setResult(result);
            return true;
        }

        // REQUIRED fields (email + employmentStartDate excluded)
        if (Utility.isNull(req.getTransId())
                || Utility.isNull(req.getMaritalStatus())
                || Utility.isNull(req.getPlaceOfBirth())
                || Utility.isNull(req.getCurrentAddressProvince())
                || Utility.isNull(req.getCurrentAddressDistrict())
                || Utility.isNull(req.getCurrentAddressWard())) {

            log.error("[DoValidateSalaryAdvanceCreate] Missing required fields");
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
            context.setResult(result);
            return true;
        }

        // Validate PlaceOfBirth details only if PlaceOfBirth is Cambodia/Campuchia
        String pob = req.getPlaceOfBirth() != null ? req.getPlaceOfBirth().toUpperCase() : "";
        if (pob.contains("CAMBODIA") || pob.contains("CAMPUCHIA") || "KHM".equals(pob) || "KH".equals(pob)) {
            if (Utility.isNull(req.getPlaceOfBirthProvince())
                    || Utility.isNull(req.getPlaceOfBirthDistrict())
                    || Utility.isNull(req.getPlaceOfBirthWard())) {
                log.error("[DoValidateSalaryAdvanceCreate] Missing place of birth details for Cambodia");
                result = new SimpleResult(
                        "Missing required fields for place of birth",
                        false,
                        ResponseCode.INVALID_INPUT.getCode()
                );
                context.setResult(result);
                return true;
            }
        }

        // Optional email validation (only if provided)
        if (!Utility.isNull(req.getEmail()) && req.getEmail().contains(" ")) {
            log.error("[DoValidateSalaryAdvanceCreate] Email INVALID");
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
            context.setResult(result);
            return true;
        }

        context.setResult(result);
        return false;
    }
}
