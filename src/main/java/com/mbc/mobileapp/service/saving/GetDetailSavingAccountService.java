
package com.mbc.mobileapp.service.saving;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.saving.DoGetDetailSavingAccount;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetDetailSavingAccountService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoGetDetailSavingAccount doGetDetailSavingAccount;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doGetDetailSavingAccount);
    }
}
