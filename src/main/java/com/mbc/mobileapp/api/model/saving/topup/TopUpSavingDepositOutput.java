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
public class TopUpSavingDepositOutput {
    private String savingId;
    private String topUpAmount;
    private String debitAmount;
    private String t24VersionId;
    private String intPostDate;
    private String remark;

}
