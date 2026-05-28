package com.mbc.mobileapp.rest.saving.topup;

import com.mbc.common.util.Constant;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class TopUpSavingInfo {

    @NotNull
    @NotBlank
    private String savingId;//so tai khoan credit

    @NotNull
    private DebitAccountTopupInfo debitAccount;// thong tin tai khoan debit

    @NotNull
    @NotBlank
    private String topUpAmount;

    @NotNull
    @NotBlank
    private String topUpCurrency;

    private String remark;

    private String branchCode = Constant.BRANCH_CODE_HO;
}
