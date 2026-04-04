package com.mbc.mobileapp.rest.saving.topup;


import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateTopUpSavingResponse extends BaseResponse {

    private String transId;

    private TopUpSavingInfo topUpSavingInfo;
}
