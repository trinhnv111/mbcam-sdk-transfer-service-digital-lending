package com.mbc.mobileapp.rest.register;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenOTPByPhoneRequest extends RestRequest {

    private String phoneNumber;
}
