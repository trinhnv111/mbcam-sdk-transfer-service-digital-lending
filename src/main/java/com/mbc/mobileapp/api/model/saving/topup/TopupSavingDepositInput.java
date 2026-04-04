package com.mbc.mobileapp.api.model.saving.topup;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopupSavingDepositInput {
    private String savingId;
    private String savingName;
    private String savingCurrency;
    private String savingType;
    private DebitAccountTopup debitAccount;
    private String topUpAmount;
    private String topUpCurrency;
    private String remark;
    private String channel;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DebitAccountTopup {
        private String accountNumber;
        private String accountName;
        private String accountType;
        private String accountCurrency;
    }
}
