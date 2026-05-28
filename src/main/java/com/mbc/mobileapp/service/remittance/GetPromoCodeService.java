package com.mbc.mobileapp.service.remittance;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.remittance.DoGetPromoCode;
import com.mbc.mobileapp.command.remittance.DoRemittanceMakeTransferFinish;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetPromoCodeService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoGetPromoCode doGetPromoCode;


    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doGetPromoCode);
    }
}
