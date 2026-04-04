package com.mbc.mobileapp.service.transfer.ciftp;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.transfer.ciftp.DoCiftpExecuteTransferToCasa;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CiftpExecuteToCasaService extends ChainBase {
    
    @Autowired
    private DoCheckRefNo doCheckRefNo;
    
    @Autowired
    private CheckCustomerState checkCustomerState;
    
    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private DoCiftpExecuteTransferToCasa doCiftpExecuteTransferToCasa;

    @Autowired
    private ValidateOTP validateOTP;
    
    @Autowired
    private DoSumLimitEasyPayment doSumLimitEasyPayment;
    
    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(validateOTP);
        addCommand(doCiftpExecuteTransferToCasa);
        addCommand(doSumLimitEasyPayment);
    }

}
