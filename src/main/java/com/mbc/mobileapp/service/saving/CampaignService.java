package com.mbc.mobileapp.service.saving;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.saving.campaign.DoGetListCampaign;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CampaignService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;
    
    @Autowired
    private CheckCustomerState checkCustomerState;
    
    @Autowired
    private DoGetListCampaign doGetListCampaign;
    
    @PostConstruct
    public void commandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doGetListCampaign);
    }
}
