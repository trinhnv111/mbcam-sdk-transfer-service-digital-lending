package com.mbc.mobileapp.rest.transfer.khqr;

import com.mbc.mobileapp.rest.bean.RestRequest;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import lombok.*;

import javax.validation.Valid;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KHQRTransferRequest extends RestRequest {

    @Valid
    private TransInfo transInfo;
}
