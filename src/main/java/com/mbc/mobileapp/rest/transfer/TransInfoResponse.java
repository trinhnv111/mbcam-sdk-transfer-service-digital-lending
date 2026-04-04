package com.mbc.mobileapp.rest.transfer;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransInfoResponse extends BaseResponse {

    private TransInfo transInfo;

    public String transId;
}
