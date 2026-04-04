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
import com.mbc.mobileapp.api.model.remittance.input.RemittanceAddressInput;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceAddressOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoGetAddressVn implements Command {
    
    @Autowired
    private CallRemittanceService apiRemittance;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        String custId = context.getCustomer().getId();
        String requestId = context.getRequest().getRequestId();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        try {
            
            RemittanceAddressInput reAddressInput = new RemittanceAddressInput();
            reAddressInput.setType(request.getType());
            reAddressInput.setCode(request.getCode());
                        
            ExecuteT24Output<List<RemittanceAddressOutput>> output= apiRemittance.getAdrressVn(reAddressInput, custId,requestId);

            if(Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())){
                List<RemittanceAddressOutput> lstAddr = output.getData();
                response.setRemittanceAddressOutput(lstAddr);                
            }else{
                String errorDesc = output.getErrorInfo().getErrorDesc();
                if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                    errorDesc =
                            output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                }
                result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());
            }

        }catch (Exception e){
            AppLog.error("[Exception Remittance Get Address] requestId: " + requestId + " desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        context.setResponse(response);
        return !result.isOk();
    }

}
