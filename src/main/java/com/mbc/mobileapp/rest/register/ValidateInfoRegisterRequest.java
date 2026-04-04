package com.mbc.mobileapp.rest.register;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class ValidateInfoRegisterRequest extends RestRequest {

    @NotNull
    @NotBlank
    private String idCardNumber;

    @NotNull
    @NotBlank
    private String idCardType;

    @NotNull
    @NotBlank
    private String phoneNumber;


    private String rmCode;

}
