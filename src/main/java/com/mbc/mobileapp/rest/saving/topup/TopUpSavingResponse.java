package com.mbc.mobileapp.rest.saving.topup;

import com.mbc.common.rest.bean.BaseResponse;

import com.mbc.mobileapp.api.model.saving.topup.TopUpSavingDepositOutput;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopUpSavingResponse extends BaseResponse {

    private TopUpSavingDepositOutput data;
}
