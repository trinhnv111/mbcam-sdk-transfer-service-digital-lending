package com.mbc.mobileapp.rest.user.pincode;

import com.mbc.common.bean.TokenOtp;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePinCodeRequest extends RestRequest {

    @NonNull
    @NotBlank
    private String oldPinCode;

    @NonNull
    @NotBlank
    private String pinCode;

    @NonNull
    @NotBlank
    private String rePinCode;

    private TokenOtp tokenOTP;
}
