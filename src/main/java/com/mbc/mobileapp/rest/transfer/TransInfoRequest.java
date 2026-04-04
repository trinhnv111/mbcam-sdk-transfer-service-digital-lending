package com.mbc.mobileapp.rest.transfer;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.*;

import javax.validation.Valid;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransInfoRequest extends RestRequest {
    @Valid
    private TransInfo transInfo;
}
