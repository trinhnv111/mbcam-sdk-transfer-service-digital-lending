package com.mbc.mobileapp.rest.saving.topup;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DebitAccountTopupInfo {
    @NotNull
    @NotBlank
    private String debitAccountNumber;

    @NotNull
    @NotBlank
    private String debitAccountName;

    @NotNull
    @NotBlank
    private String debitAccountType;

    @NotNull
    @NotBlank
    private String debitAccountCurrency;
}
