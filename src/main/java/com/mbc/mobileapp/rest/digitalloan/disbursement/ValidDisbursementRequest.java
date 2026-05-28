package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ValidDisbursementRequest extends BaseRequest {
    @NotBlank
    @NotNull
    private String transId;
}
