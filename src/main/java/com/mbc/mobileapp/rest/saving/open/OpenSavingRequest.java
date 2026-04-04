package com.mbc.mobileapp.rest.saving.open;

import com.mbc.common.bean.TokenOtp;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenSavingRequest extends RestRequest {

    @NotNull
    @NotBlank
    private String transId;

    @Valid
    private TokenOtp tokenOTP;
}
