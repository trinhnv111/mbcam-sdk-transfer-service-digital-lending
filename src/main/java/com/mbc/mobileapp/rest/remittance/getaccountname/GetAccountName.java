package com.mbc.mobileapp.rest.remittance.getaccountname;

import lombok.Data;

@Data
public class GetAccountName {
    private String accountNumber;
    private String benBank;
    private String accountName;
    private String accountCurrency;
}
