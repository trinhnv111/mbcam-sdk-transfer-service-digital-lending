package com.mbc.mobileapp.api.model.digitalloan.input;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MsLoanCreateRequest {

    // Customer info
    private String customerCode;
    private String customerName;
    private String customerNationalId;
    private String rmCode;
    private String phoneNumber;
    private String occupation;
    private String selfEmployment;
    private String employmentDate;
    private BigDecimal monthlySalary;
    private String salaryCurrency;

    // Nested loan info
    private LoanInfo loanInfo;

    @Data
    public static class LoanInfo {
        private BigDecimal loanAmount;
        private String loanCurrency;
        private BigDecimal loanInterest;
        private BigDecimal loanInterestSpread;
        private String valueDate;
        private String maturityDate;
        private String channel;
        private String product;
        private String subProduct;
        private String partnerCode;
    }
}