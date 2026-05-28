package com.mbc.mobileapp.rest.saving.close;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DepositCloseRequest extends RestRequest {
    @NotNull
    @NotBlank
    private String transId;

    @Valid
    private TokenOtp tokenOTP;
}
