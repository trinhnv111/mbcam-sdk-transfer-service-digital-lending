package com.mbc.mobileapp.rest.remittance.init;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MakeTransferInitResponse extends BaseResponse {

    private String transId;
    private InitMakeTransferInfo initMakeTransferInfo;
}
