
package com.mbc.mobileapp.service.transfer.inhouse;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.transfer.DoExecuteTransfer;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MakeTransferService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;
    
    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private DoExecuteTransfer doMakeTransfer;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private ValidateOTP validateOTP;
    
//    @Autowired
//    private DoPlusLoyaltyPoints doPlusLoyaltyPoints;
    
    @Autowired
    private DoSumLimitEasyPayment doSumLimitEasyPayment;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(validateOTP);
        addCommand(doMakeTransfer);
        addCommand(doSumLimitEasyPayment);
//        addCommand(doPlusLoyaltyPoints);
    }
}
