package com.mbc.mobileapp.service.user.initsdk;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.user.initsdk.DoCheckPartner;
import com.mbc.mobileapp.command.user.initsdk.DoInitSdk;
import com.mbc.mobileapp.command.user.initsdk.DoValidateToken;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class InitSdkService extends ChainBase {


    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoCheckPartner doCheckPartner;

    @Autowired
    private DoValidateToken doValidateToken;

    @Autowired
    private DoInitSdk doInitSdk;


    @PostConstruct
    public void addCommandChain() {

        addCommand(doCheckRefNo);
        addCommand(doCheckPartner);
        addCommand(doValidateToken);
        addCommand(doInitSdk);
    }
}
