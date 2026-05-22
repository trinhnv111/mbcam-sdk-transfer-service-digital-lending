package com.mbc.mobileapp.api.model.salary_advance.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryAdvanceOfferLimitData {
    private BigDecimal approveLimit = BigDecimal.ZERO;
    private BigDecimal usedLimit = BigDecimal.ZERO;
    private BigDecimal remainingLimit =  BigDecimal.ZERO;
    private String currency;
    private String endDate;
}
