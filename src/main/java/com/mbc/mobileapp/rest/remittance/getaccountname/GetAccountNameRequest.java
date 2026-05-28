package com.mbc.mobileapp.rest.remittance.getaccountname;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GetAccountNameRequest extends RestRequest {
    @NotNull
    @NotBlank
    private String accountNo;

    @NotNull
    @NotBlank
    private String benBank;
}
