package com.mbc.mobileapp.rest.remittance.finish;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceMakeTransferFinishOutput;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MakeTransferFinishResponse extends BaseResponse {
    private RemittanceMakeTransferFinishOutput data;

}
