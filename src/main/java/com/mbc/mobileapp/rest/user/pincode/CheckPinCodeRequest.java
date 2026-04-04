package com.mbc.mobileapp.rest.user.pincode;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckPinCodeRequest extends RestRequest {

    @NonNull
    @NotBlank
    private String idCardNumber;
}
