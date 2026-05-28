package com.mbc.mobileapp.rest.saving.close;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateDepositClosureRequest extends RestRequest {
    private DepositClosureInfo depositClosureInfo;
}
