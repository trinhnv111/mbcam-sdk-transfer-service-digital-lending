package com.mbc.mobileapp.rest.saving.close;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateDepositClosureResponse extends BaseResponse {
    private String transId;
}
