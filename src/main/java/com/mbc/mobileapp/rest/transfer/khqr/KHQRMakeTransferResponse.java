package com.mbc.mobileapp.rest.transfer.khqr;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KHQRMakeTransferResponse extends BaseResponse {

    private TransInfo transInfo;

    private String transId;

    private String traceCode;

    private String transHash;
}
