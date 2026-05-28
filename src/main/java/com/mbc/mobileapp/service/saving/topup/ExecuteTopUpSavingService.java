package com.mbc.mobileapp.service.saving.topup;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.common.command.ValidateOTP;
import com.mbc.common.command.pushnotifypartner.DoPushNotifyPartner;
import com.mbc.mobileapp.command.saving.topup.DoExecuteTopUpSaving;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class ExecuteTopUpSavingService extends ChainBase {
    private final DoCheckRefNo doCheckRefNo;

    private final DoCheckSrvc doCheckSrvc;

    private final CheckCustomerState checkCustomerState;

    private final ValidateOTP validateOTP;

    private final DoExecuteTopUpSaving doExecuteTopUpSaving;

    private final DoPushNotifyPartner doPushNotifyPartner;

    @PostConstruct
    public void addCommand() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(validateOTP);
        addCommand(doExecuteTopUpSaving);
        addCommand(doPushNotifyPartner);
    }
}
