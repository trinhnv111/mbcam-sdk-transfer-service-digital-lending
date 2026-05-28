package com.mbc.mobileapp.rest.common.otp;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOtpRequest extends RestRequest {

    private String transId;

    private GenOtpCustInfo custInfo;

}
