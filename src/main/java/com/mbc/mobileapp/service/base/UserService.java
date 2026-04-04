package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.Request;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.user.LoginResponse;
import com.mbc.mobileapp.rest.user.initsdk.InitSdkResponse;
import com.mbc.mobileapp.rest.user.pincode.*;

public interface UserService {

    public InitSdkResponse initSdk(Request request);

    public LoginResponse doLogin(Request request);

    public SetPinCodeResponse setPinCode(Request request, CustInfo cust, TokenOtp otp);

    public CheckPinCodeResponse checkPinCode(Request request, CustInfo cust);

    public CheckPinCodeResponse clearPinCode(Request request, CustInfo cust);

    public ChangePinCodeResponse changePinCode(Request request, CustInfo cust, TokenOtp otp);

    public ForgotPinCodeResponse forgotPinCode(Request request, CustInfo cust);

    public ResetPinCodeResponse resetPinCode(Request request, CustInfo cust, TokenOtp otp);
}
