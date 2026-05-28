package com.mbc.mobileapp.rest.account.history.bakong;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GetDetailTransactionHistoryRq extends RestRequest {
    @NotNull(message = "transCode is not null")
    @NotBlank(message = "transCode is not blank")
    private String transCode;
}
