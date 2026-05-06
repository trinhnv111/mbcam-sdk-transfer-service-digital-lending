package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.mobileapp.api.model.salary_advance.output.CustInfoOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryAdvanceInitData {
    private String transId;
    private CustInfoOutput custInfo;
}
