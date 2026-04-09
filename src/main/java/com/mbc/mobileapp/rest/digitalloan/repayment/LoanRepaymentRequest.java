package com.mbc.mobileapp.rest.digitalloan.repayment;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentRequest extends BaseRequest {

    @ApiModelProperty(notes = "Tai khoan over draft", required = true, example = "1995740750")
    @NotBlank(message = "odAccountNo is mandatory")
    private String odAccountNo;

    @ApiModelProperty(notes = "So tien thanh toan du no", required = true, example = "2")
    @NotBlank(message = "amountRepayment is mandatory")
    private String amountRepayment;

    @ApiModelProperty(notes = "Loai tien", required = true, example = "USD")
    @NotBlank(message = "amountCurrency is mandatory")
    private String amountCurrency;

    @ApiModelProperty(notes = "Tai khoan thanh toan cua khach hang", required = true, example = "1100027457")
    @NotBlank(message = "odAccountNo is mandatory")
    private String debitAccount;

    @ApiModelProperty(notes = "Ten tai khoan thanh toan cua khach hang", required = true, example = "NGUYEN VIET HOANG")
    @NotBlank(message = "debitAccountName is mandatory")
    private String debitAccountName;

    @ApiModelProperty(notes = "Loai tien cua tai khoan thanh toan", required = true, example = "USD")
    @NotBlank(message = "accountCurrency is mandatory")
    private String debitAccountCurrency;

    //    @Valid
    //    @NotNull(message = "tokenOTP is mandatory")
    private TokenOtp tokenOTP;
}
