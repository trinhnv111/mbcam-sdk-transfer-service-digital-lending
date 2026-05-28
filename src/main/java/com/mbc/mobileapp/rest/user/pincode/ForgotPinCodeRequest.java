package com.mbc.mobileapp.rest.user.pincode;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPinCodeRequest extends RestRequest {

    @NonNull
    @NotBlank
    private String custName;

    @NonNull
    @NotBlank
    private String idCardNumber;

//    @NonNull
//    @NotBlank
    private String deviceName;

    private String phoneNumber;


}
