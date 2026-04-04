
package com.mbc.mobileapp.rest.transfer;

import com.mbc.common.bean.TokenOtp;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;

@Getter
@Setter
public class MakeTransferRequest extends RestRequest {

    @Valid
    private MakeTransferInfo transInfo;

    private TokenOtp tokenOTP;

}
