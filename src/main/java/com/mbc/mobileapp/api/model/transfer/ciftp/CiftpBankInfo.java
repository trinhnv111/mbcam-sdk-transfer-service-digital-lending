package com.mbc.mobileapp.api.model.transfer.ciftp;

import com.mbc.mobileapp.api.model.transfer.BankInfo;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CiftpBankInfo extends BankInfo {
    
    private String bakongAccID;
    private String logoUuid;
    private String ncsBankCode;
    private List<String> publicOperations;
}
