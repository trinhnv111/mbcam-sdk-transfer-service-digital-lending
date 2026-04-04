package com.mbc.mobileapp.rest.saving.campaign;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSavingReponse extends BaseResponse {
    
    private List<CampaignConfig> lstCampaign;

}
