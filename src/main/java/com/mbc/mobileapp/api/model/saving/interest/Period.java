package com.mbc.mobileapp.api.model.saving.interest;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Period {
    private String restPeriod;
    private String restDate;
    private String daysSinceSpot;
    private BigDecimal bidRate;
    private BigDecimal offerRate;
}
