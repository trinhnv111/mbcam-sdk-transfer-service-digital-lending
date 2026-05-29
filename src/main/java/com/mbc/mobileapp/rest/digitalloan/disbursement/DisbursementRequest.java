package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DisbursementRequest extends BaseRequest {

    @NotBlank
    @NotNull
    private String transId;

    @NotBlank
    @NotNull
    private String disburseAmount;

    private String currency;

    @NotBlank
    @NotNull
    private String selectedAccountNumber;

    @NotBlank
    @NotNull
    private String disbursementType;

    private String selectedAccountName;

    private TokenOtp tokenOTP;
}

