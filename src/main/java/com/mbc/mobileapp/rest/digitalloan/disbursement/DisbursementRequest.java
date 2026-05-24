package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DisbursementRequest extends BaseRequest {

    @NotBlank
    @NotNull
    private String transId;

    /** Số tiền muốn giải ngân (khách nhập vào slider) */
    @NotBlank
    @NotNull
    private String disburseAmount;

    /** Loại tiền tệ: USD / KHR */
    private String currency;

    /**
     * Số tài khoản / ví được chọn để nhận tiền.
     * TH1: Số tài khoản thanh toán MBC
     * TH2: Số ví eMoney
     */
    @NotBlank
    @NotNull
    private String selectedAccountNumber;

    /**
     * Loại tài khoản đích: "MBC_ACCOUNT" | "EMONEY_WALLET"
     * Dùng để rẽ nhánh Make Transfer TH1 vs TH2
     */
    @NotBlank
    @NotNull
    private String disbursementType;

    /** Tên tài khoản/ví người nhận */
    private String selectedAccountName;

    /** OTP token để validate trước khi giải ngân */
    private com.mbc.common.bean.TokenOtp tokenOTP;
}

