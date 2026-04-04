/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mbc.mobileapp.api.model.saving.account;

import com.mbc.common.il.base.ExecuteT24MessageInput;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingAccountListInput extends ExecuteT24MessageInput {
    private String customerId;
    private String accountId;
    private String accountTypes;  
}
