package com.mbc.mobileapp.rest.digitalloan.getloan;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PaymentRequest {
    @NotBlank
    @NotNull
    private String loanId;
    private String fromDate;
    private String toDate;
}
