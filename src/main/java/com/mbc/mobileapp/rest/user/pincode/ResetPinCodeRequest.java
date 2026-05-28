package com.mbc.mobileapp.rest.user.pincode;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ResetPinCodeRequest extends RestRequest {

    @NotBlank
    @NotNull
    private String transId;

    @NonNull
    @NotBlank
    private String pinCode;

    @NonNull
    @NotBlank
    private String rePinCode;

    @Valid
    private TokenOtp tokenOTP;
}
