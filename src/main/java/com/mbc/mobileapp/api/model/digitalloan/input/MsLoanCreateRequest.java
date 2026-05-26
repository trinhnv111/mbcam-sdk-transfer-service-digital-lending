package com.mbc.mobileapp.api.model.digitalloan.input;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Tương ứng với API "Create Loan" — SDK BE → MS Loan → T24
 */
@Data
@Builder
public class MsLoanCreateRequest {
    // Thông tin khách hàng

    private String customerCode;

    private String customerName;

    private String phoneNumber;

    private String occupation;

    private String employmentDate;

    private BigDecimal monthlySalary;

    private String salaryCurrency;

    // Thông tin khoản vay (loanInfo)

    private BigDecimal loanAmount;

    private String loanCurrency;

    private BigDecimal loanInterest;

    private BigDecimal loanInterestSpread;

    private String valueDate;

    private String maturityDate;

    private String product;

    private String subProduct;

    private String channel;

    private String partnerCode;
}
