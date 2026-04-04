package com.mbc.mobileapp.api.model.transfer.ciftp;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AccountInquiryOutput {

    private String accountNumber;
    private String accountName;
    private String accountCurrency;
    private String bankCode;
    private String accountType;
    
}
