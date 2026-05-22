package com.mbc.mobileapp.api.model.salary_advance.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SaLimitData {
    private BigDecimal approveLimit;
    private BigDecimal usedLimit;
    private BigDecimal remainingLimit;
    private String currency;
//    private Boolean isDisabilities;
//    private String startDate;
    private String endDate;

}
