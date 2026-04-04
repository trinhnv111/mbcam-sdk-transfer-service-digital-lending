package com.mbc.mobileapp.api.model.transfer.ciftp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountInQuiryInput {
    
    private String accountNumber;
    private String participantCode;
    private String transferType;

}
