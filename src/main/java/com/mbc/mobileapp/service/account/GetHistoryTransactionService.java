package com.mbc.mobileapp.service.account;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.account.DoGetHistoryTransaction;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetHistoryTransactionService extends ChainBase {
    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoGetHistoryTransaction doGetHistoryTransaction;

    @PostConstruct
    public void addCommandChain() {

        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doGetHistoryTransaction);
    }
}
