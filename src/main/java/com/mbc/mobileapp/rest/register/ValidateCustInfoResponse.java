package com.mbc.mobileapp.rest.register;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateCustInfoResponse extends BaseResponse {

    private String custId;

    private RegisterCustInfo registerInfo;
}
