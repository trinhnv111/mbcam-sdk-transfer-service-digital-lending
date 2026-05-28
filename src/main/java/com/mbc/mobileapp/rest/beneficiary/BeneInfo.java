package com.mbc.mobileapp.rest.beneficiary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneInfo {

    @NotBlank
    @NotNull
    private String benAcctNo;

    private String benAcctName;

    private String benBankCode;

    private String benBankName;

    private String benBankMnemonic;

    private String suggestName;

    private String beneficiaryId;

    private String currency;

    private String transType;

    private String type;

    private String partner;

}
