package com.mbc.mobileapp.service.register;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.register.DoGenOTPByPhone;
import com.mbc.mobileapp.command.register.DoValidIdentityCard;
import com.mbc.mobileapp.command.register.DoValidPhoneNo;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ValidateInfoRegisterService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoValidPhoneNo doValidPhoneNo;

    @Autowired
    private DoValidIdentityCard doValidIdentityCard;

//    @Autowired
//    private DoGenOTPByPhone doGenOTPByPhone;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doValidPhoneNo);
        addCommand(doValidIdentityCard);
//        addCommand(doGenOTPByPhone);

    }
}
