package com.mbc.mobileapp.service.transfer.ciftp;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.transfer.ciftp.DoCiftpAccountInquiryCasa;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CiftpAccountInquiryCasaService extends ChainBase {
    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoCiftpAccountInquiryCasa doCiftpAccountInquiryCasa;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCiftpAccountInquiryCasa);
    }
}
