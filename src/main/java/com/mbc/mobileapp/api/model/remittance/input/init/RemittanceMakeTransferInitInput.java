package com.mbc.mobileapp.api.model.remittance.input.init;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemittanceMakeTransferInitInput {
    private String remittancePaymentType;
    private BeneInfo beneInfo;
    private String debitCurrency;
    private String creditCurrency;
    private BigDecimal amount;
    private String partnerCode;
    private String transactionMessage;
    private String transactionPurpose;
    private Remitter remitter;
}
