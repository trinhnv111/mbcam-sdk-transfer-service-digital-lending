package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.constant.TransactionAuthMethod;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

@Service
public class DoMapSalaryAdvanceOTP implements Command {
    @Override
    public boolean execute(Context cntxt) throws Exception {
//        ProcessContext context = (ProcessContext) cntxt;
//        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
//        SalaryAdvanceCreateRequest createRequest = request.getSalaryAdvanceCreateRequest();
//
//        TokenOtp otp = new TokenOtp();
////        otp.setOtpValue(createRequest.getOtp());
//        otp.setAuthMethod(TransactionAuthMethod.AUTH_METHOD_SMS);
//        otp.setOtpType("SMS");
//
//        context.putVar(Constant.KeyVar.OTP, otp);
//        context.setResult(Validator.Result.OK);
        return false;
    }
}
