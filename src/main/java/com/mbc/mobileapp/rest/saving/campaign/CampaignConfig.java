package com.mbc.mobileapp.rest.saving.campaign;

import com.mbc.common.entity.ComCampaignDeposit;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignConfig {

    private String id;
        
    private String campaignCode;
  
    private String campaignName;

    private String description;

    private String status;

    private Date startDate;

    private Date endDate;

    private String noDec;

    private String partnerCode;

    private String productType;

    private String volume;

    private String channel;
    
    private List<ComCampaignDeposit> detailCampaign;
}
