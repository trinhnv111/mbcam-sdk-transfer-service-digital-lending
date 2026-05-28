package com.mbc.mobileapp.rest.saving.cob;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.saving.cob.CheckCoBOutput;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckCoBResponse extends BaseResponse {
    private CheckCoBOutput data;
}
