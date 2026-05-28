package com.mbc.mobileapp.service.saving.open;

import com.mbc.common.command.*;
import com.mbc.common.command.pushnotifypartner.DoPushNotifyPartner;
import com.mbc.mobileapp.command.saving.open.DoOpenSaving;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class OpenSavingService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private ValidateOTP validateOTP;

    @Autowired
    private DoOpenSaving doOpenSaving;

    @Autowired
    private DoSumLimitEasyPayment doSumLimitEasyPayment;

    @Autowired
    private DoPushNotifyPartner doPushNotifyPartner;

    @PostConstruct
    public void addCommand(){
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(validateOTP);
        addCommand(doOpenSaving);
        addCommand(doSumLimitEasyPayment);
        addCommand(doPushNotifyPartner);
    }

}
