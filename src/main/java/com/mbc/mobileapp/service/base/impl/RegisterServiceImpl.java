package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.register.*;
import com.mbc.mobileapp.service.base.RegisterService;
import com.mbc.mobileapp.service.register.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RegisterServiceImpl extends ServiceBase implements RegisterService {

    @Autowired
    private ValidateInfoRegisterService validateInfoRegisterService;

    @Autowired
    private GenOTPByPhoneService genOTPByPhoneService;

    @Autowired
    private ValidateOTPByPhoneService validateOTPByPhoneService;

    @Autowired
    private ValidateCustomerInfoService validateCustomerInfoService;

    @Autowired
    private CreateCustomerService createCustomerService;


    @Override
    public ValidateInfoRegisterResponse validateInfoRegister(Request request, CustInfo cust) {
        ProcessContext ctx = loadContext(request, null);
        try {
            validateInfoRegisterService.execute(ctx);
            logService.execute(ctx);
        }
        catch (Exception ex) {
            AppLog.error(ex);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();

        CommonServiceResponse response = (CommonServiceResponse) ctx.getResponse();

        ValidateInfoRegisterResponse resp = new ValidateInfoRegisterResponse();
        resp.setCurrency(response.getLstCurrency());
        TokenOtp otp = (TokenOtp) ctx.getVar("otp");
//			otp.setOtpValue(null);
        resp.setTokenOTP(otp);
        resp.setResult(result);
        return resp;
    }

    @Override
    public GenOTPByPhoneResponse genOtpByPhone(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, null);

        try {
            genOTPByPhoneService.execute(context);
            logService.execute(context);
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        GenOTPByPhoneResponse response = new GenOTPByPhoneResponse();
        Validator.Result result = context.getResult();
        if (result.isOk()) {
            TokenOtp otp = (TokenOtp) context.getVar("otp");
//			otp.setOtpValue(null);
            response.setTokenOTP(otp);
        }
        response.setResult(result);
        return response;
    }

    public ValidateOTPByPhoneResponse validateOTPByPhone(Request request, TokenOtp otp) {
        ProcessContext context = loadContext(request, null);
        context.putVar("otp", otp);
        try {
            validateOTPByPhoneService.execute(context);
            logService.execute(context);

        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        ValidateOTPByPhoneResponse response = new ValidateOTPByPhoneResponse();
        Validator.Result result = context.getResult();
        response.setResult(result);
        return response;
    }

    @Override
    public ValidateCustInfoResponse validateCustInfo(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, null);
        try {
            validateCustomerInfoService.execute(context);
            logService.execute(context);

        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        CommonServiceRequest req = (CommonServiceRequest) context.getRequest();

        ValidateCustInfoResponse response = new ValidateCustInfoResponse();
        Validator.Result result = context.getResult();
        if(result.isOk()){
            response.setRegisterInfo(req.getRegisterCustInfo());
            CustInfo custInfo = (CustInfo) context.getVar(Constant.KeyVar.CUSTOMER_INFO);
            response.setCustId(custInfo.getId());
        }

        response.setResult(result);
        return response;
    }

    @Override
    public CreateCustomerResponse createCustomer(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, null);
//        context.putVar(Constant.KeyVar.OTP, otp);
        CreateCustomerResponse resposne = new CreateCustomerResponse();
        Validator.Result result = null;
        try {
            //fix pentest
            TimeUnit.MILLISECONDS.sleep(20);
            createCustomerService.execute(context);
            logService.execute(context);
//            logCreateCustomerService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                resposne.setCustId(context.getVar("custId").toString());
            }

            resposne.setResult(result);

        }
        catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
            resposne.setResult(Validator.Result.UNKNOWN);
        }
        return resposne;
    }
}
