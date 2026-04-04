package com.mbc.mobileapp.api.model.transfer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankInfo {
    
    private String bankName;
    
    private String bankCode;
    
    private String participantCode;
    
    private String bicfiCode;
    
    private String shortName;

    private String clientMessageId;

}
