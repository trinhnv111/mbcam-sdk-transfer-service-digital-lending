package com.mbc.mobileapp.rest.register;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenOTPByPhoneResponse extends BaseResponse {

    public TokenOtp tokenOTP;
}
