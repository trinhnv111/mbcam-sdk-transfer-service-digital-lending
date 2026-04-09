package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.digitalloan.output.MsLoanGetPdOutput;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetPdResponse extends BaseResponse {
    private MsLoanGetPdOutput data;
}
