package com.mbc.mobileapp.service.remittance;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.remittance.DoGetAddressVn;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetAddressVnService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;
    
    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoGetAddressVn doGetAddressVn;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doGetAddressVn);

    }
}
