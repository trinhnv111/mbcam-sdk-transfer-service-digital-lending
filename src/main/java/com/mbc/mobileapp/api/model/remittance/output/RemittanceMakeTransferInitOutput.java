package com.mbc.mobileapp.api.model.remittance.output;

import lombok.Data;

import java.util.List;

@Data
public class RemittanceMakeTransferInitOutput  {
    private String requestMessageId;
    private String beneAccountName;
    private String benAmount;
    private String beneAccountCurrency;
    private String feeMBC;
    private String feeMBCCurrency;
    private String rate;
    private List<RemittanceDiscount> discounts;
}
