package com.mbc.mobileapp.rest.transfer.khqr;

import com.mbc.common.bean.TokenOtp;
import com.mbc.mobileapp.rest.bean.RestRequest;
import com.mbc.mobileapp.rest.transfer.MakeTransferInfo;
import lombok.*;

import javax.validation.Valid;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KHQRMakeTransferRequest extends RestRequest {

    @Valid
    private MakeTransferInfo transInfo;

    private TokenOtp tokenOTP;
}
