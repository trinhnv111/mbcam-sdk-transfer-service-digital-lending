package com.mbc.mobileapp.api.model.saving.interest;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InterestRate {
    private String currency;
    private String description;
    private List<Period> restPeriodList;

    //add info for flexi term

    private BigDecimal minAmountDeposit;
    private BigDecimal minAmountTopUp;
    private BigDecimal maxAmountDeposit;
    private BigDecimal interestRateMax;
    private BigDecimal totalAmountDeposit;
}

