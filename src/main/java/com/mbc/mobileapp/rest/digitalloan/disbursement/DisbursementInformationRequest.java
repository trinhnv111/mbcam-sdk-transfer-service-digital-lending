package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Data;
import javax.validation.constraints.NotBlank;


@Data
public class DisbursementInformationRequest extends BaseRequest {

    @NotBlank
    private String transId;
    @NotBlank
    private String referLoanAmount;
    @NotBlank
    private String selectedAmountNo;
    @NotBlank
    private String disbursementAccountType;

    private String referrerPhone;


}
