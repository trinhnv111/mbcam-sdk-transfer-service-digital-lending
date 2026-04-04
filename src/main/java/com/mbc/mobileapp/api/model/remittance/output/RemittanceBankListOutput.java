package com.mbc.mobileapp.api.model.remittance.output;

import lombok.Data;

@Data
public class RemittanceBankListOutput {
    private String bankCode;
    private String bankName;
    private String bankShortName;
    private String biccode;
}
