package com.mbc.mobileapp.rest.saving.open;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateSavingResponse extends BaseResponse {

    private String transId;

    private SavingInfo savingInfo;
}
