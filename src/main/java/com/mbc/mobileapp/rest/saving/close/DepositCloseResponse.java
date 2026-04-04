package com.mbc.mobileapp.rest.saving.close;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.saving.close.DepositClosureOutput;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepositCloseResponse extends BaseResponse {
    private DepositClosureOutput data;
}
