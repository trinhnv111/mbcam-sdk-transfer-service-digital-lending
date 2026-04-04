package com.mbc.mobileapp.api.model.transfer.ciftp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CiftpChargeOutput {
    
    private String chargeCode;
    private String chargeAmount;
    private String chargeCurrency;

}
