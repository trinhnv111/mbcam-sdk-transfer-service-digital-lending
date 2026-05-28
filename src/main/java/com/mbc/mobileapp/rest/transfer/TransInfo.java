package com.mbc.mobileapp.rest.transfer;

import com.mbc.mobileapp.rest.transfer.ciftp.CiftpChannelLimit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransInfo {

//    //TRUE: NGUOI CHUYEN CHIU PHI,  FALSE: NGUOI HUONG CHIU PHI
    private boolean ownerCharge;

    // LOAI TAI KHOAN CHUYEN
//    private String debitAcctType;

    // TAI KHOAN CHUYEN
    @NotBlank
    @NotNull
    private String debitAcctNo;

    @Schema(hidden = true)
    private String debitAcctName;

    // LOAI TIEN CHUYEN THEO TK DEBIT
    @Schema(hidden = true)
    private String debitCurrency;

    // TAI KHOAN NHAN
    @NotBlank
    @NotNull
    private String creditAcctNo;

    // TEN TAI KHOAN NHAN
    @NotBlank
    @NotNull
    private String creditAcctName;

    // LOAI TIEN NHAN
    @NotBlank
    @NotNull
    private String creditCurrency;

    //MA NGAN HANG THU HUONG
    private String  destBankCode;

    //TEN NGAN HANG THU HUONG
    private String destBankName;

    // MA PARTICIPANT CODE NGAN HANG THU HUONG
//    @NotBlank
//    @NotNull
    private String destBankPartiCode;

    // TEN CHI NHANH NGAN HANG NHAN
    private String destBranchName;

    //MA CHI NHANH NGUOI CHUYEN
//    @NotBlank
//    @NotNull
    @Schema(hidden = true)
    private String branchCode;

    //MO TA GIAO DICH
    @NotBlank
    @NotNull
    private String description;

    @NotBlank
    @NotNull
    private String amount;

    @NotBlank
    @NotNull
    private String currency;

    //TY GIA 1/RATE T24
    @Schema(hidden = true)
    private BigDecimal exchangeRate;

    // TY GIA THEO T24
    @Schema(hidden = true)
    private String rate;

    // SO TIEN CHUYEN
    @Schema(hidden = true)
    private String debitAmount;

    // SO TIEN NHAN
    @Schema(hidden = true)
    private String creditAmount;

    // SO TIEN PHI
    @Schema(hidden = true)
    private String chargeAmount;

    // LOAI TIEN PHI
    @Schema(hidden = true)
    private String chargeCurrency;

    // SO TIEN PHI CAT TU TAI KHOAN CHUYEN
    private String debitChargeAmount;

    @Schema(hidden = true)
    private String transactionType;

    private String payloadQr;

    private String qrPayType;

    private String ciftpChannel;

    private List<String> ciftpChannelAvailable;

    private String paymentTypeCode;

    private CiftpChannelLimit ciftpChannelLimit;

    private String transferType;

    private String bakongAcctId;

}
