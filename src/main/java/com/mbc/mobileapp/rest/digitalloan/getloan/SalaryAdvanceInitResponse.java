package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.salary_advance.output.CustInfoOutput;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryAdvanceInitResponse extends BaseResponse {
    private CustInfoOutput custInfo;
}
