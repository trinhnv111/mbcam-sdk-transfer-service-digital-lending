package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DisbursementRequest extends BaseRequest {
    @NotBlank
    @NotNull
    private String transId;
    private TokenOtp tokenOTP;
}
