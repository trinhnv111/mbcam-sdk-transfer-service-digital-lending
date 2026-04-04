package com.mbc.mobileapp.command.transfer.ciftp;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallFundsTransferService;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpMakeConfig;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpMakeConfigInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DoCiftpMakeTransferConfig implements Command {
    
    @Autowired
    private CallFundsTransferService callFundsTransferService;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        try {
            
            CiftpMakeConfig ciftpMakeConfig = CiftpMakeConfig.builder().service(request.getCiftpTransferType()).build();
            ExecuteT24Output<List<CiftpMakeConfigInfo>> output =
                    callFundsTransferService.makeTransferConfig(ciftpMakeConfig, customer.getId(), request.getRequestId());
            
            if(Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                response.setLstMakeTransferConfig(output.getData());

            }else {
                String errorDesc = output.getErrorInfo().getErrorDesc();
                if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                    errorDesc =
                            output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                }
                result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());
            }
        }
        catch (Exception e) {
            log.error("[SDK Exception Make Transfer Config] requestId: {}, data: {}", request.getRequestId(), e.getStackTrace());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
