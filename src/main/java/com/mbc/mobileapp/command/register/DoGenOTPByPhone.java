package com.mbc.mobileapp.command.register;

import com.mbc.common.api.ApiOtpShare;
import com.mbc.common.api.models.otp.OtpShareInput;
import com.mbc.common.api.models.otp.TokenOtpDataOutput;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DoGenOTPByPhone implements Command {
    
    @Autowired
    private ApiOtpShare apiOtpShare;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        Validator.Result result = Validator.Result.OK;

        try {

            OtpShareInput input = new OtpShareInput();
            input.setLifeTime("120000");
            input.setOtpSize("6");
            input.setType("0");
            input.setUserId(request.getPhoneNo());

            ExecuteT24Output<TokenOtpDataOutput> output =
                apiOtpShare.getOtpShare(input, null, request.getRequestId());

            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                TokenOtp otp = new TokenOtp();
                otp.setOtpValue(output.getData().getOtpValue());
                otp.setOtpType(input.getType());
                context.putVar("otp", otp);
                    
                String sms = "Please enter OTP " + output.getData().getOtpValue()
                        + " to confirm the transaction. The OTP will expire after 2 minutes. Please do not provide this OTP code to anyone.";                        
                context.putVar(Constant.KeyVar.SMS_PHONE_NO, request.getPhoneNo());
                context.putVar(Constant.KeyVar.SMS_MESSAGE_CONTENT, sms);
            }
            else {

                result = new SimpleResult(
                    output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail(), false,
                    output.getErrorInfo().getErrorCode());
            }
        }
        catch (Exception e) {
            result = new SimpleResult(ResponseCode.OTP_GENERATE_FAIL.getDesc(), false,
                ResponseCode.OTP_GENERATE_FAIL.getCode());
            AppLog.error(e);
        }
        context.setResult(result);
        return !result.isOk();
    }
}
