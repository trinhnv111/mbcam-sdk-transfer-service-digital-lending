package com.mbc.mobileapp.rest.remittance.addr;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemittanceAddressRequest extends RestRequest {
    
    private String type;
    private String code;
}
