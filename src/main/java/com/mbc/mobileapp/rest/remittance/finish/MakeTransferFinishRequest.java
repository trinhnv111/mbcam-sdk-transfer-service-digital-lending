package com.mbc.mobileapp.rest.remittance.finish;

import com.mbc.common.bean.TokenOtp;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MakeTransferFinishRequest extends RestRequest {
    @NotNull
    @NotBlank
    private String transId;

    private TokenOtp tokenOTP;


}
