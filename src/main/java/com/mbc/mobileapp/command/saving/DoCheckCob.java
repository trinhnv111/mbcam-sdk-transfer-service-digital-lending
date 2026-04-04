package com.mbc.mobileapp.command.saving;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallSavingService;
import com.mbc.mobileapp.api.model.saving.cob.CheckCoBInput;
import com.mbc.mobileapp.api.model.saving.cob.CheckCoBOutput;
import com.mbc.mobileapp.config.SavingFixedDepositConfig;
import com.mbc.mobileapp.constant.SavingDepositConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DoCheckCob implements Command {

    private final CallSavingService callSavingService;


    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        String custId = context.getCustomer().getId();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        try {
            CheckCoBInput input = new CheckCoBInput();
            input.setVersion(SavingDepositConstant.SavingDepositType.VERSION_TSA_IL);
            input.setDestination(SavingDepositConstant.SavingDepositType.DESTINATION_IL);
            input.setAction(SavingDepositConstant.SavingDepositType.ACTION_TSA_IL);
            input.setBranchCode(SavingFixedDepositConfig.BRANCH_CODE);

            ExecuteT24Output<CheckCoBOutput> output = callSavingService.checkCoB(input, custId, request.getRequestId());
            if (Objects.isNull(output)) {
                AppLog.error(" DoCheckCoB timeout ");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                CheckCoBOutput checkCoBOutput = output.getData();
                response.setCheckCoBOutput(checkCoBOutput);
                if(!"stop".equalsIgnoreCase(checkCoBOutput.getServiceControl())){
                    result = new SimpleResult(MBCResponseCode.CHECK_COB_FAIL.getDesc(), false, MBCResponseCode.CHECK_COB_FAIL.getCode());
                }
            } else {
                result = new SimpleResult(
                        output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail(),
                        false, output.getErrorInfo().getErrorCode());
            }
            context.setResult(result);
            return !result.isOk();

        } catch (Exception e) {
            AppLog.error("[SDK Exception Get Saving Account] requestId: "+request.getRequestId()+" desc: " , e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        return !result.isOk();
    }
}
