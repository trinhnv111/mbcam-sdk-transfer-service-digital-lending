package com.mbc.mobileapp.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum KycType {
    
    BASIC_TYPE("0", "BASIC"),
    EKYC_TYPE("1", "PARTIAL"),
    FULL_KYC_TYPE("2", "KYC.FULL");    
    
    @Getter
    private String code;
    
    @Getter
    private String message;

}
