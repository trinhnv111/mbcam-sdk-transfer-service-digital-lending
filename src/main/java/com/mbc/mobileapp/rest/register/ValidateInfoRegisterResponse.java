package com.mbc.mobileapp.rest.register;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class ValidateInfoRegisterResponse extends BaseResponse {

    private List<String> currency;

    public TokenOtp tokenOTP;
}
