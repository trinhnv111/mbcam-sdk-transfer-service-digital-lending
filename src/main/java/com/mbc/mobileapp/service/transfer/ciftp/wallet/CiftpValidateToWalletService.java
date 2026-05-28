package com.mbc.mobileapp.service.transfer.ciftp.wallet;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.transfer.ciftp.wallet.DoCiftpAccountInquiryWallet;
import com.mbc.mobileapp.command.transfer.ciftp.wallet.DoCiftpValidTransferToWallet;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CiftpValidateToWalletService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private DoCiftpAccountInquiryWallet doCiftpAccountInquiryWallet;

    @Autowired
    private DoCheckDebitAccount doCheckDebitAccount;

    @Autowired
    private DoCiftpValidTransferToWallet doValidTransferToWallet;


    @Autowired
    private DoGetLimitEasyPaymentUsed doGetLimitEasyPaymentUsed;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doCheckDebitAccount);
        addCommand(doCiftpAccountInquiryWallet);
        addCommand(doValidTransferToWallet);
        addCommand(doGetLimitEasyPaymentUsed);
    }
}
