package com.mbc.mobileapp.rest.register;

import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateCustInfoRequest extends RestRequest {

    private RegisterCustInfo registerInfo;
}
