
package com.mbc.mobileapp.service.common;

import com.mbc.common.command.*;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GenerateOTPService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;
    
    @Autowired
    private DoCheckCustomerGenOtp doCheckCustomerGenOtp;
    
    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private GenerateOTP generateOTP;
    
    @Autowired
    private DoSendSmsMessage doSendSmsMessage;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);       
        addCommand(doCheckCustomerGenOtp);
        addCommand(doCheckSrvc);
        addCommand(generateOTP);
        addCommand(doSendSmsMessage);
    }
}
