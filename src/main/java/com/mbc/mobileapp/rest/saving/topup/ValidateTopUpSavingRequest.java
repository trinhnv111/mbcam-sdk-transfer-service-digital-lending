package com.mbc.mobileapp.rest.saving.topup;

import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateTopUpSavingRequest extends RestRequest {

    private TopUpSavingInfo topUpSavingInfo;
}
