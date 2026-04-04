package com.mbc.mobileapp.command.remittance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallRemittanceService;
import com.mbc.mobileapp.api.model.remittance.input.RemittanceGetAccountNameInput;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.remittance.getaccountname.GetAccountName;
import com.mbc.mobileapp.rest.remittance.getaccountname.GetAccountNameRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoRemittanceGetAccountName implements Command {

    private final CallRemittanceService apiRemittance;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        String custId = context.getCustomer().getId();
        String requestId = context.getRequest().getRequestId();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        GetAccountNameRequest getAccountNameRequest = request.getGetAccountNameRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        try {
            RemittanceGetAccountNameInput accountNameRequest = new RemittanceGetAccountNameInput();
            accountNameRequest.setPartnerCode(CommonServiceConstant.Channel.MBC_MOBILE.name());
            accountNameRequest.setAccountNo(getAccountNameRequest.getAccountNo());
            accountNameRequest.setBenBank(getAccountNameRequest.getBenBank());

            ExecuteT24Output<GetAccountName> output = apiRemittance.getAccountName(accountNameRequest, custId, requestId);

            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                GetAccountName getAccountNameOutput = output.getData();
                response.setGetAccountNameOutput(getAccountNameOutput);
                context.setResult(result);
                context.setResponse(response);
                return !result.isOk();
            } else if ("421".equals(output.getSoaErrorCode())) {
                result = new SimpleResult(MBCResponseCode.BENEFICIARY_BANK_INCORRECT.getDesc(), false, MBCResponseCode.BENEFICIARY_BANK_INCORRECT.getCode());
            } else if(!"4900".equals(output.getSoaErrorCode())){
                result = new SimpleResult(MBCResponseCode.NOT_FOUND_ACCOUNT.getDesc(), false, MBCResponseCode.NOT_FOUND_ACCOUNT.getCode());
            }else{
                String errorDesc = output.getErrorInfo().getErrorDesc();
                if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                    errorDesc = output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                }
                result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());
            }
        } catch (Exception e) {
            AppLog.error("ERROR: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
