package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Request cho POST /digital-loan/genfile
 */
@Data
public class GenFileRequest extends BaseRequest {
    @NotBlank
    @NotNull
    private String transId; // transId là ID của ComTransDtlLoanRegistration (được trả về từ /get-confirm)
}
