package com.mbc.mobileapp.service.transfer.ciftp;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.transfer.DoCiftpGetListBank;
import com.mbc.mobileapp.command.transfer.ciftp.DoCiftpAccountInquiryCasa;
import com.mbc.mobileapp.command.transfer.ciftp.DoCiftpMakeTransferConfig;
import com.mbc.mobileapp.command.transfer.ciftp.DoCiftpValidTransferToCasa;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CiftpValidToCasaService extends ChainBase {
    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private DoCheckDebitAccount doCheckDebitAccount;
    
    @Autowired
    private DoCiftpGetListBank doCiftpGetListBank;
    
    @Autowired
    private DoCiftpMakeTransferConfig doCiftpMakeTransferConfig;

    @Autowired
    private DoCiftpValidTransferToCasa doCiftpValidTransferToCasa;
    
    @Autowired
    private DoCiftpAccountInquiryCasa doCiftpAccountInquiryCasa;
    
    @Autowired
    private DoGetLimitEasyPaymentUsed doGetLimitEasyPaymentUsed;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doCheckDebitAccount);
        addCommand(doCiftpGetListBank);
        addCommand(doCiftpMakeTransferConfig);
        addCommand(doCiftpAccountInquiryCasa);
        addCommand(doCiftpValidTransferToCasa);
        addCommand(doGetLimitEasyPaymentUsed);
    }
}
