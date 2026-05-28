package com.mbc.mobileapp.service.register;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoSendSmsMessage;
import com.mbc.mobileapp.command.register.DoGenOTPByPhone;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GenOTPByPhoneService extends ChainBase {

    @Autowired
    DoCheckRefNo doCheckRefNo;

    @Autowired
    DoGenOTPByPhone doGenOTPByPhone;

    @Autowired
    DoSendSmsMessage doSendSmsMessage;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doGenOTPByPhone);
        addCommand(doSendSmsMessage);
    }
}
