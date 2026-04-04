package com.mbc.mobileapp.api.model.remittance.input;

import lombok.Data;

@Data
public class RemittanceGetAccountNameInput  {
    private String partnerCode;

    private String accountNo;

    private String benBank;
}
