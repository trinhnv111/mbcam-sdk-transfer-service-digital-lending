package com.mbc.mobileapp.rest.transfer.khqr;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KHQRTransferResponse extends BaseResponse {

    private TransInfo transInfo;

    public String transId;
}
