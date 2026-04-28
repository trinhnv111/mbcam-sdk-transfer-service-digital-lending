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
        SalaryAdvanceCreateRequest createRequest = request.getSalaryAdvanceCreateRequest();

        if (createRequest == null) {
            log.error("[DoValidateSalaryAdvanceCreate] Request body is empty");
            result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
            context.setResult(result);
            return !result.isOk();
        }

        if (Utility.isNull(createRequest.getTempRecordId())) {
            log.error("[DoValidateSalaryAdvanceCreate] tempRecordId is null or empty");
            result = new SimpleResult("tempRecordId is required", false, ResponseCode.INVALID_INPUT.getCode());
            context.setResult(result);
            return !result.isOk();
        }

        if (!Utility.isNull(createRequest.getEmail())) {
            if (createRequest.getEmail().contains(" ")) {
                log.error("[DoValidateSalaryAdvanceCreate] Email contains whitespace");
                result = new SimpleResult("Invalid email format", false, ResponseCode.INVALID_INPUT.getCode());
                context.setResult(result);
                return !result.isOk();
            }
        }

        context.setResult(result);
        return !result.isOk();
    }
}
