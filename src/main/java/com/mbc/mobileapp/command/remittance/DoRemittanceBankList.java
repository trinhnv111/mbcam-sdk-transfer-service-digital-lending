package com.mbc.mobileapp.command.remittance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallRemittanceService;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceBankListOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoRemittanceBankList implements Command {

    private final CallRemittanceService apiRemittance;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context=(ProcessContext) cntxt;
        Validator.Result result=Validator.Result.OK;
        String custId=context.getCustomer().getId();
        String requestId=context.getRequest().getRequestId();
        CommonServiceResponse response=(CommonServiceResponse) context.getResponse();

        try {
            ExecuteT24Output<List<RemittanceBankListOutput>> output= apiRemittance.getBankList(custId,requestId);

            if(Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())){
                List<RemittanceBankListOutput> bankList=output.getData();
                response.setRemittanceBankListOutputList(bankList);
                context.setResult(result);
                context.setResponse(response);
                return !result.isOk();
            }else{
                String errorDesc = output.getErrorInfo().getErrorDesc();
                if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                    errorDesc =
                            output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                }
                result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());
            }

        }catch (Exception e){
            AppLog.error("[Exception Remittance Get List Bank] requestId: "+requestId+" desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
