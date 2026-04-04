package com.mbc.mobileapp.service.transfer.ciftp.wallet;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.transfer.ciftp.wallet.DoCiftpExecuteTransferToWallet;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CiftpExecuteToWalletService extends ChainBase {
    
    @Autowired
    private DoCheckRefNo doCheckRefNo;
    
    @Autowired
    private CheckCustomerState checkCustomerState;
    
    @Autowired
    private DoCheckSrvc doCheckSrvc;
    
    @Autowired
    private ValidateOTP validateOTP; 
    
    @Autowired
    private DoCiftpExecuteTransferToWallet doCiftpExecuteTransferToWallet;
    
    @Autowired
    private DoSumLimitEasyPayment doSumLimitEasyPayment;
    
    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(validateOTP);
        addCommand(doCiftpExecuteTransferToWallet);
        addCommand(doSumLimitEasyPayment);
    }

}
