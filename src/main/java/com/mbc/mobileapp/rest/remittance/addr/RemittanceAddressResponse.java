package com.mbc.mobileapp.rest.remittance.addr;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceAddressOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RemittanceAddressResponse extends BaseResponse {
    
    private List<RemittanceAddressOutput> data;
}
