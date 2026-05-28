package com.mbc.mobileapp.rest.register;

import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;

@Getter
@Setter
public class CreateCustomerRequest extends RestRequest {

    private String custId;

    @Valid
    private RegisterCustInfo registerInfo;
}
