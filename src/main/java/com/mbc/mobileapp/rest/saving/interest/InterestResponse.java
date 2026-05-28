package com.mbc.mobileapp.rest.saving.interest;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.saving.interest.InterestOutput;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterestResponse extends BaseResponse {
    private InterestOutput data;
}
