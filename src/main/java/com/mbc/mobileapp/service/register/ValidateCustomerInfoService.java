package com.mbc.mobileapp.service.register;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.register.DoValidIdentityCard;
import com.mbc.mobileapp.command.register.DoValidPhoneNo;
import com.mbc.mobileapp.command.register.DoValidateCustInfo;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ValidateCustomerInfoService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoValidateCustInfo doValidateCustInfo;


    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doValidateCustInfo);

    }
}
