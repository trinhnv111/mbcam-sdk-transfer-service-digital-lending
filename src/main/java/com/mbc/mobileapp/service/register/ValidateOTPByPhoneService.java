package com.mbc.mobileapp.service.register;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.ValidateOTP;
import com.mbc.mobileapp.rest.register.DoLogValidOtpPhoneNo;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ValidateOTPByPhoneService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private ValidateOTP validateOTP;

    @Autowired
    private DoLogValidOtpPhoneNo doLogValidOtpPhoneNo;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(validateOTP);
        addCommand(doLogValidOtpPhoneNo);
    }
}
