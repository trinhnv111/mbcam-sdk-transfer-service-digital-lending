package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.dto.PartnerSdkResponse;
import com.mbc.common.entity.ComDeviceLoginHist;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComDeviceLoginHistRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.user.LoginCustomerInfo;
import com.mbc.mobileapp.rest.user.LoginResponse;
import com.mbc.mobileapp.rest.user.initsdk.InitSdkResponse;
import com.mbc.mobileapp.rest.user.pincode.*;
import com.mbc.mobileapp.service.base.UserService;
import com.mbc.mobileapp.service.user.LoginService;
import com.mbc.mobileapp.service.user.initsdk.InitSdkService;
import com.mbc.mobileapp.service.user.pincode.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Service
public class UserServiceImpl extends ServiceBase implements UserService {

    @Autowired
    private InitSdkService initSdkService;

    @Autowired
    private LoginService loginservice;

    @Autowired
    private ComDeviceLoginHistRepo comDeviceLoginHistRepo;

    @Autowired
    private SetPinCodeService setPinCodeService;

    @Autowired
    private CheckPinCodeService checkPinCodeService;

    @Autowired
    private ClearPinCodeService clearPinCodeService;

    @Autowired
    private ChangePinCodeService changePinCodeService;

    @Autowired
    private ForgotPinCodeService forgotPinCodeService;

    @Autowired
    private ResetPinCodeService resetPinCodeService;

    @Override
    public InitSdkResponse initSdk(Request request) {
        ProcessContext ctx = loadContext(request, null);
//        InitSdkResponse resp = new InitSdkResponse();
        try {
            initSdkService.execute(ctx);
            logService.execute(ctx);
        } catch (Exception ex) {
            AppLog.error(ex);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        CommonServiceResponse response = (CommonServiceResponse) ctx.getResponse();
        PartnerSdkResponse partnerSdk = response.getPartnerSdk();
        InitSdkResponse resp = InitSdkResponse.builder()
                .custInfo(response.getCustomerInfoInitSdk())
                .tid(Objects.nonNull(response.getTid()) ? response.getTid() : "")
                .partner(partnerSdk.getPartnerCode())
                .color(partnerSdk.getColor())
                .description(partnerSdk.getDescription())
                .email(partnerSdk.getEmail())
                .features(partnerSdk.getFeatureConfigList())
                .channel(response.getChannel())
                .build();

        resp.setResult(result);
        return resp;
    }

    @Override
    public LoginResponse doLogin(Request request) {
        ProcessContext ctx = loadContext(request, null);
        try {
            loginservice.execute(ctx);
            logService.execute(ctx);
        } catch (Exception ex) {
            AppLog.error(ex);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        LoginResponse resp = new LoginResponse();

        CustInfo customer = ctx.getCustomer();
        if (customer != null) {
            customer.setTimestamp(String.valueOf(new Date().getTime()));
            if (result.isOk() || ResponseCode.PASSWORD_MUST_CHANGE.getCode().equals(result.getResponseCode())
                    || ResponseCode.PASSWORD_IS_EXPIRED.getCode().equals(result.getResponseCode())) {
                resp.setCust(customer);
                LoginCustomerInfo info = mapper.convertValue(customer, LoginCustomerInfo.class);
                resp.setCustInfo(info);
            }


            CommonServiceRequest svRequest = (CommonServiceRequest) ctx.getRequest();
            ComDeviceLoginHist comDeviceLoginHist = new ComDeviceLoginHist();
            comDeviceLoginHist.setUserId(customer.getUserId());
            comDeviceLoginHist.setCustId(customer.getId());
            comDeviceLoginHist.setDeviceId(svRequest.getDeviceId());
            comDeviceLoginHist.setStatus(customer.getImUserStatus());
            comDeviceLoginHist.setLoginCount(new BigDecimal(0));
            comDeviceLoginHist.setChannel(svRequest.getDigitalChannel());
            comDeviceLoginHist.setIpAddress(svRequest.getIpAddress());
            comDeviceLoginHist.setPhoneModel(svRequest.getPhoneId());
            comDeviceLoginHist.setUserAgent(svRequest.getHeaderAgent());
            comDeviceLoginHist.setCreatedBy(customer.getNm());
            comDeviceLoginHist.setLoginRespCode(result.getResponseCode());
            comDeviceLoginHist.setLoginRespDesc(result.getMessage());
            comDeviceLoginHistRepo.saveAndFlush(comDeviceLoginHist);
        }

        resp.setResult(result);
        return resp;
    }

    @Override
    public SetPinCodeResponse setPinCode(Request request, CustInfo cust, TokenOtp otp) {
        ProcessContext ctx = loadContext(request, cust);
        ctx.putVar(Constant.KeyVar.OTP, otp);

        try {
            setPinCodeService.execute(ctx);
            logService.execute(ctx);
        } catch (Exception ex) {
            AppLog.error(ex);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        SetPinCodeResponse resp = new SetPinCodeResponse();

        resp.setResult(result);
        return resp;
    }

    @Override
    public CheckPinCodeResponse checkPinCode(Request request, CustInfo cust) {
        ProcessContext ctx = loadContext(request, cust);
        try {
            checkPinCodeService.execute(ctx);
            logService.execute(ctx);
        } catch (Exception ex) {
            AppLog.error(ex);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        CheckPinCodeResponse resp = new CheckPinCodeResponse();

        resp.setResult(result);
        return resp;
    }

    @Override
    public CheckPinCodeResponse clearPinCode(Request request, CustInfo cust) {
        ProcessContext ctx = loadContext(request, cust);
        try {
            clearPinCodeService.execute(ctx);
            logService.execute(ctx);
        } catch (Exception ex) {
            AppLog.error(ex);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        CheckPinCodeResponse resp = new CheckPinCodeResponse();

        resp.setResult(result);
        return resp;
    }

    @Override
    public ChangePinCodeResponse changePinCode(Request request, CustInfo cust, TokenOtp otp) {
        ProcessContext ctx = loadContext(request, cust);
        ctx.putVar(Constant.KeyVar.OTP, otp);

        try {
            changePinCodeService.execute(ctx);
            logService.execute(ctx);
        } catch (Exception ex) {
            AppLog.error(ex);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        ChangePinCodeResponse resp = new ChangePinCodeResponse();

        resp.setResult(result);
        return resp;
    }

    @Override
    public ForgotPinCodeResponse forgotPinCode(Request request, CustInfo cust) {
        ProcessContext ctx = loadContext(request, cust);

        try {
            forgotPinCodeService.execute(ctx);
            logService.execute(ctx);
        } catch (Exception ex) {
            AppLog.error(ex);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        ForgotPinCodeResponse resp = new ForgotPinCodeResponse();
        resp.setTransId(ctx.getResponse().getTransId());

        resp.setResult(result);
        return resp;
    }

    @Override
    public ResetPinCodeResponse resetPinCode(Request request, CustInfo cust, TokenOtp otp) {
        ProcessContext ctx = loadContext(request, cust);
        ctx.putVar(Constant.KeyVar.OTP, otp);

        try {
            resetPinCodeService.execute(ctx);
            logService.execute(ctx);
        } catch (Exception ex) {
            AppLog.error(ex);
            ctx.setResult(Validator.Result.UNKNOWN);
        }
        Validator.Result result = ctx.getResult();
        ResetPinCodeResponse resp = new ResetPinCodeResponse();

        resp.setResult(result);
        return resp;
    }
}
