package com.mbc.mobileapp.service.account;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.account.DoGetDetailHistoryBakongGW;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetDetailHistoryBakongGWService extends ChainBase {
    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoGetDetailHistoryBakongGW doGetDetailHistoryBakongGW;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doGetDetailHistoryBakongGW);
    }
}
