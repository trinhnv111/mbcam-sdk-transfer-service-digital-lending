package com.mbc.mobileapp.service.saving.close;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.common.command.ValidateOTP;
import com.mbc.common.command.pushnotifypartner.DoPushNotifyPartner;
import com.mbc.mobileapp.command.saving.close.DoDepositClosure;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DepositClosureService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private ValidateOTP validateOTP;

    @Autowired
    private DoDepositClosure doDepositClosure;

    @Autowired
    private DoPushNotifyPartner doPushNotifyPartner;

    @PostConstruct
    public void addCommand() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(validateOTP);
        addCommand(doDepositClosure);
        addCommand(doPushNotifyPartner);
    }
}
