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
public class SetPinCodeRequest extends RestRequest {

    @NonNull
    @NotBlank
    private String idCardNumber;

    @NonNull
    @NotBlank
    private String phoneNumber;

    @NonNull
    @NotBlank
    private String pinCode;

    @NonNull
    @NotBlank
    private String rePinCode;

    private TokenOtp tokenOTP;
}
