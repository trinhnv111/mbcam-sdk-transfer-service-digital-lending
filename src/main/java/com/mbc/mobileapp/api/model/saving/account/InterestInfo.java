package com.mbc.mobileapp.api.model.saving.account;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InterestInfo {
    private String interestType;
    private String interestRate;
    private String interestPaymentType;
    private String interestPaymentInterval;
    private String intRatePreclose;
    private String amtAccr;
    private String amtIntPreclosure;
    private String amtIntEndCapDate;

    private BigDecimal approxNetMonthlyInterest;

    private String timePreclose;
    private InterestRatePreClose intRatePrecloseRT;
    private String precloseOrigInt;
    private String precloseIntCorr;
    private String precloseRedIntAmt;
    private String precloseTotalAmtDue;

    @Getter
    @Setter
    public static class InterestRatePreClose {
        private String term;
        private String nonTerm;
    }
}
