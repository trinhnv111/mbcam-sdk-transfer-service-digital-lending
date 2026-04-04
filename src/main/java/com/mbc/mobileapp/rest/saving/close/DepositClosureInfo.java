package com.mbc.mobileapp.rest.saving.close;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DepositClosureInfo {
    @NotNull
    @NotBlank
    private String savingCurrency;
    @NotNull
    @NotBlank
    private String receivingCurrency;
    @NotBlank
    @NotNull
    private String receivingAccount;
    @NotBlank
    @NotNull
    private String branchCode;
    @NotBlank
    @NotNull
    private String savingType;
//    @NotBlank
//    @NotNull
    private String interestOption;
    @NotBlank
    @NotNull
    private String savingAcctNo;
    @NotNull
    @NotBlank
    private String requestId;
}
