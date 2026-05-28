package com.mbc.mobileapp.service.transfer.ciftp.wallet;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.transfer.ciftp.DoCiftpAccountInquiryCasa;
import com.mbc.mobileapp.command.transfer.ciftp.wallet.DoCiftpAccountInquiryWallet;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CiftpAccountInquiryWalletService extends ChainBase {
    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoCiftpAccountInquiryWallet doCiftpAccountInquiryWallet;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCiftpAccountInquiryWallet);
    }
}
