package com.mbc.mobileapp.service.saving.open;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.saving.campaign.DoCheckCampaign;
import com.mbc.mobileapp.command.saving.open.DoValidateInterestRate;
import com.mbc.mobileapp.command.saving.open.DoValidateOpenSaving;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ValidateOpenSavingService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoCheckSrvc doCheckSrvc;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoValidateInterestRate doValidateInterestRate;

    @Autowired
    private DoCheckCampaign doCheckCampaign;

    @Autowired
    private DoValidateOpenSaving doValidateOpenSaving;

    @Autowired
    private DoGetLimitEasyPaymentUsed doGetLimitEasyPaymentUsed;



    @PostConstruct
    public void addCommand(){
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doValidateInterestRate);
        addCommand(doCheckCampaign);
        addCommand(doValidateOpenSaving);
//        addCommand(doGetLimitEasyPaymentUsed);
    }

}
