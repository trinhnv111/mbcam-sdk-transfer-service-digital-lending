package com.mbc.mobileapp.rest.digitalloan.getloan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalaryAdvanceCreateData {
    private String transId;
    private Double limitAmount;
    private String currency;
}