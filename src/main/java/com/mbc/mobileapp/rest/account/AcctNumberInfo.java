package com.mbc.mobileapp.rest.account;

import com.mbc.common.services.il.nonsavingacct.BranchInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AcctNumberInfo {
    
    private String accountNo;
    private String accountType;
    private String accountCurrency;
    private String customerName;
    private BranchInfo branchInfo;
    private String custId;
    
}
