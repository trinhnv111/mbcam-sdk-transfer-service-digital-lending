package com.mbc.mobileapp.repository;

import com.mbc.common.entity.ComCampaignDeposit;
import com.mbc.common.repository.ComCampaignDepositRepo;

import java.util.List;

public interface ComCampaignDepositExtendRepo extends ComCampaignDepositRepo{

    public List<ComCampaignDeposit> findByDepositCampaignId(String depositCampaignId);
}
