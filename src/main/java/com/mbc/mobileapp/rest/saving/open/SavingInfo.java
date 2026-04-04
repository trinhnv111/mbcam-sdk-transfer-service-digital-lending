package com.mbc.mobileapp.rest.saving.open;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingInfo {

    @NotNull
    @NotBlank
    private String productCode;

    @NotNull
    @NotBlank
    private String amount;

    @NotNull
    @NotBlank
    private String currency;

    @NotNull
    @NotBlank
    private String debitAccount;

    // @NotNull
    // @NotBlank
    private String beneficiaryAccount;

    @NotNull
    @NotBlank
    private String branchCode;


    @NotNull
    @NotBlank
    private String maturityInstruction;

    @NotNull
    @NotBlank
    private String term;

    @NotBlank
    @NotNull
    private String interestRate;

//    @NotNull
//    @NotBlank
    @Schema(hidden = true)
    private String taxRate;

    @Schema(hidden = true)
    private BigDecimal matureAmount;

    //Lãi dự tính nhận được nếu tất toán đúng hạn
    @Schema(hidden = true)
    private BigDecimal estimatedInterest;

    //thuế dự tính
    private BigDecimal estimatedTax;

    //lãi nhận hàng tháng
    private BigDecimal approxNetMonthlyInterest ;

    private String referrerPhoneNumber;

    private String referrerName;

    private String referrerCif;

    private String savingType;

    /*
     * required if deposit flexi term
     */
    // private String repayAccountName;
    // private String repayAccountCurrency;
    // private String repayAccountType;

    /*
     * new field 07/05
     */
    private String rmCode;

    private String partnerCode;

    private String campaignCode;

    private String campaignInterest;

    private String campaignVolume;


}
