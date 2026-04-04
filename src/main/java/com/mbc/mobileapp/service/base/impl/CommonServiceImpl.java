package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.common.otp.GenerateOtpResponse;
import com.mbc.mobileapp.rest.common.token.GenerateTokenResponse;
import com.mbc.mobileapp.service.base.CommonService;
import com.mbc.mobileapp.service.common.GenerateOTPService;
import com.mbc.mobileapp.service.common.GenerateTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonServiceImpl extends ServiceBase implements CommonService {

    @Autowired
    private GenerateOTPService generateOTPService;

    @Autowired
    private GenerateTokenService generateTokenService;

    @Override
    public GenerateOtpResponse generateOTP(Request request, CustInfo cust) {
        ProcessContext ctx = loadContext(request, cust);
        try {
            generateOTPService.execute(ctx);
            logService.execute(ctx);
        }
        catch (Exception e) {
            AppLog.error(e);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        GenerateOtpResponse resp = new GenerateOtpResponse();
        if (result.isOk()) {
            TokenOtp otp = (TokenOtp) ctx.getVar("otp");
//            otp.setOtpValue(null);
            resp.setTokenOTP(otp);
        }
        resp.setResult(result);
        return resp;
    }

    @Override
    public GenerateTokenResponse generateToken(Request request, CustInfo cust) {
        ProcessContext ctx = loadContext(request, cust);
        try {
            generateTokenService.execute(ctx);
            logService.execute(ctx);
        }
        catch (Exception e) {
            AppLog.error(e);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        GenerateTokenResponse resp = new GenerateTokenResponse();
        if (result.isOk()) {
            CommonServiceResponse response = (CommonServiceResponse) ctx.getResponse();
//            otp.setOtpValue(null);
            resp.setToken(response.getToken());
        }
        resp.setResult(result);
        return resp;
    }
}
