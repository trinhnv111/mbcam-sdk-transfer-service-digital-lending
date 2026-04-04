package com.mbc.mobileapp.rest.user.pincode;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPinCodeResponse extends BaseResponse {

    private String transId;
}
