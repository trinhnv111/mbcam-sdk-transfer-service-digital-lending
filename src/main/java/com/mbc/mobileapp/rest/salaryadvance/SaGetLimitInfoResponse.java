package com.mbc.mobileapp.rest.salaryadvance;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaGetLimitInfoResponse extends BaseResponse {
    private String limitCode;
    private String productType;
    private BigDecimal limitAmount;
    private BigDecimal usedAmount;
    private BigDecimal availableAmount;
    private String currency;
    private BigDecimal fixedFee;
    private String limitStatus;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private Boolean hasActiveLoan;
    private String activeLoanCode;
}