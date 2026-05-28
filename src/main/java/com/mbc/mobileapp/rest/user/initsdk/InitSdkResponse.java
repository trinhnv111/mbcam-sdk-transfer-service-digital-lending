
package com.mbc.mobileapp.rest.user.initsdk;

import com.mbc.common.dto.PartnerFeatureDTO;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitSdkResponse extends BaseResponse {

    private CustomerInfo custInfo;
    private String tid;
    private String partner;

    private String color;
    private String description;
    private String email;
    private List<PartnerFeatureDTO> features;

    private String channel;

}
