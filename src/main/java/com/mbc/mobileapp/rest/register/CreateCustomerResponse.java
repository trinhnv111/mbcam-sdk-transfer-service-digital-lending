package com.mbc.mobileapp.rest.register;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCustomerResponse extends BaseResponse {

    private String custId;
}
