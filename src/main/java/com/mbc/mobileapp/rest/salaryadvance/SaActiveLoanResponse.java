package com.mbc.mobileapp.rest.salaryadvance;


import lombok.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class SaActiveLoanResponse {
    private String loanCode;
    private String loanStatus;
    private BigDecimal grossAmount;
    private BigDecimal outstanding;
    private BigDecimal principalPaid;
    private LocalDate disbursementDate;
    private LocalDate dueDate;
    private BigDecimal feeAmount;
    private String collectionStatus;
}
