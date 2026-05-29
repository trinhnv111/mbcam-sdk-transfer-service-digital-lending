package com.mbc.mobileapp.api.model.digitalloan.output;

import lombok.Data;


@Data
public class MsLoanCreateOutput {

    private String customerId;

    private String transactionId;

    private String limitId;

    private String creditContractId;

    private String ldId;

    private String loanAmount;

    private String loanTerm;

    private String loanInterest;

    private String loanFee;

    private String drawdownAccount;

    private String drawdownAccountName;

    private String drawdownAccountCurrency;

    private String actualLoanAmount;

    private String valueDate;

    private String maturityDate;
}
