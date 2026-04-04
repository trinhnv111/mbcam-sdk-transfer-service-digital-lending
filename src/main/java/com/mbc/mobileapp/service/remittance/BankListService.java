package com.mbc.mobileapp.service.remittance;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.remittance.DoRemittanceBankList;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class BankListService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

//    private final DoCheckSrvc doCheckSrvc;
    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoRemittanceBankList doRemittanceBankList;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
//        addCommand(doCheckSrvc);
        addCommand(doRemittanceBankList);

    }
}
