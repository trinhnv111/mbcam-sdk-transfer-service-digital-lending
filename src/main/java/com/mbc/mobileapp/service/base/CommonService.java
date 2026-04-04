package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.Request;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.common.otp.GenerateOtpResponse;
import com.mbc.mobileapp.rest.common.token.GenerateTokenResponse;

public interface CommonService {

    public GenerateOtpResponse generateOTP(Request request, CustInfo cust);

    public GenerateTokenResponse generateToken(Request request, CustInfo cust);
}
