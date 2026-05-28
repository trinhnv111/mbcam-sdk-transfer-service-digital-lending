package com.mbc.mobileapp.rest.remittance.init;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class InitMakeTransferInfo {
    //TRUE: NGUOI CHUYEN CHIU PHI,  FALSE: NGUOI HUONG CHIU PHI
    private boolean ownerCharge;

    private String transactionType;

    // LOAI TAI KHOAN CHUYEN
    private String debitAcctType;

    // TAI KHOAN CHUYEN
    @NotBlank
    @NotNull
    private String debitAcctNo;

    // TEN TAI KHOAN CHUYEN
    @NotBlank
    @NotNull
    private String debitAcctName;

    // LOAI TIEN CHUYEN
    private String debitCurrency;

    // SO TIEN CHUYEN
    private String debitAmount;

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

    // SO TIEN NHAN
    private String creditAmount;

    //SO TIEN NHAP
    @NotBlank
    @NotNull
    private String amount;

    //LOAI TIEN NHAP
    @NotBlank
    @NotNull
    private String currency;

    // SO TIEN PHI
    private String chargeAmount;

    // LOAI TIEN PHI
    private String chargeCurrency;

    // TY GIA
    private String exchangeRate;

    // TEN NGAN HANG THU HUONG
    @NotBlank
    @NotNull
    private String destBankName;

    // MA NGAN HANG THU HUONG
    @NotBlank
    @NotNull
    private String destBankCode;

    // MA CHI NHANH NGUOI CHUYEN
    @NotBlank
    @NotNull
    private String branchCode;

    //MO TA GIAO DICH
    private String description;

    //MUC DICH CHUYEN TIEN
    @NotBlank
    @NotNull
    private String purpose;

    private String chargeCode;

    @NotBlank
    @NotNull
    private String national;
    
   //DIA CHI NGUOI THU HUONG  
    @NotBlank
    @NotNull
    private String benAddress;
    
    private String discountCode;
    
    private String discountAmount;
    
    private String promoCode;

}
