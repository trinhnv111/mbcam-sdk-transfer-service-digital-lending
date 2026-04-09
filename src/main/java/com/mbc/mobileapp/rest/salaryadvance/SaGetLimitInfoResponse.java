package com.mbc.mobileapp.rest.salaryadvance;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaGetLimitInfoResponse extends BaseResponse implements Serializable {
    private Boolean hasLimit;
    private String limitCode;
    private String productType;
    private BigDecimal limitAmount;
    private BigDecimal usedAmount;
    private BigDecimal availableAmount;
    private String currency;
    private BigDecimal fixedFee;
    private String limitStatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date effectiveDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expiryDate;
//    private Boolean hasActiveLoan;
//    private String activeLoanCode;
    private SaActiveLoanResponse saActiveLoanResponse;
}