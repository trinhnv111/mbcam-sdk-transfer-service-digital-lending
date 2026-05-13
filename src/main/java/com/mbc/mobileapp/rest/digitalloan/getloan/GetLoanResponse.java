package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.digitalloan.output.GetLoanOutput;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetLoanResponse extends BaseResponse {
//    private Object data;
    private GetLoanOutput data;
    private String t24DayNow;
}
