package com.mbc.mobileapp.api.model.saving.account.detail;

import com.mbc.common.il.base.ExecuteT24MessageInput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetSavingAccountListInput extends ExecuteT24MessageInput {
    private String customerId;
    private String accountId;
    private String accountTypes;  
}
