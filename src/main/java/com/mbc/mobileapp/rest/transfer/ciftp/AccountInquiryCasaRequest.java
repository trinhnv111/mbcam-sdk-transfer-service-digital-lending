package com.mbc.mobileapp.rest.transfer.ciftp;

import com.mbc.mobileapp.rest.bean.RestRequest;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AccountInquiryCasaRequest extends RestRequest {
        @ApiParam(example = "", required = true)
        @NotNull
        @NotBlank
        private String accountNumber;

        @ApiParam(example = "", required = true)
        @NotNull
        @NotBlank
        private String participantCode;
}
