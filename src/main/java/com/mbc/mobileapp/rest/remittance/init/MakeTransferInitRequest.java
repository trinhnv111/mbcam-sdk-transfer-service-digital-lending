package com.mbc.mobileapp.rest.remittance.init;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MakeTransferInitRequest extends RestRequest {
       private InitMakeTransferInfo initMakeTransferInfo;
}
