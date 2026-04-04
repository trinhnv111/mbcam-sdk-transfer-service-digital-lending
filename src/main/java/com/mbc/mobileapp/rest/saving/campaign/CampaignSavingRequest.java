package com.mbc.mobileapp.rest.saving.campaign;

import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSavingRequest extends RestRequest {
    
    private String productType;

}
