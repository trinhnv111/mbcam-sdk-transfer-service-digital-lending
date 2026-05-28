package com.mbc.mobileapp.service.remittance;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.remittance.DoRemittanceGetAccountName;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetAccountNameService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private DoRemittanceGetAccountName doRemittanceGetAccountName;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doRemittanceGetAccountName);
    }
}
