package com.mbc.mobileapp.rest.common.otp;


import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOtpResponse extends BaseResponse {

    public TokenOtp tokenOTP;
}
