package com.mbc.mobileapp.service.remittance;

import com.mbc.common.command.*;
import com.mbc.common.command.pushnotifypartner.DoPushNotifyPartner;
import com.mbc.mobileapp.command.remittance.DoRemittanceMakeTransferFinish;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class TransferFinishService extends ChainBase {
    private final DoCheckRefNo doCheckRefNo;

    private final CheckCustomerState checkCustomerState;

    private final DoCheckSrvc doCheckSrvc;

    private final ValidateOTP validateOTP;

    private final DoRemittanceMakeTransferFinish doRemittanceMakeTransferFinish;

    private final DoSumLimitEasyPayment doSumLimitEasyPayment;

    private final DoPushNotifyPartner doPushNotifyPartner;

//    private final DoCheckCampaignRemittance doCheckCampaignRemittance;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(validateOTP);
        addCommand(doRemittanceMakeTransferFinish);
        addCommand(doSumLimitEasyPayment);
        addCommand(doPushNotifyPartner);
//        addCommand(doCheckCampaignRemittance);
    }
}
