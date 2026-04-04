package com.mbc.mobileapp.api.model.saving.open;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpenSavingInput {
    private String productCode;
    private String accountOfficer;
    private String accountTitle;
    private String channel;
    private String currency;
    private String customer;
    private String coCode;
    private InterestPaymentInterval interestPaymentInterval;
    private String interestRate;
    private String jointHolder;
    private String jointNotes;
    private String locTerm;
    private Integer maturityInstr;
    private String mbLdType;
    private String mnemonic;
    private String nominateAccount;
    private String payIntAtMat;
    private BigDecimal principalAmt;
    private String productGrCode;
    private String relationCode;
    private String repayAccount;
    private String rmCrossSell;
    private String shortTitle;
    private Boolean maturityFromCache;
    private String requestId;
    private String rolloverIntRate;

    /*
     * required if deposit flexi term
     * */
    private String repayAccountName;
    private String repayAccountCurrency;
    private String repayAccountType;

    /*
     * new field 07/05
     * */
    private String refRmCode;
    private String refPartCode;


}
