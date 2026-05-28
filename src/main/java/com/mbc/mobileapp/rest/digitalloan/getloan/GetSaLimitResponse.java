package com.mbc.mobileapp.rest.digitalloan.getloan;


import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.salary_advance.output.SalaryAdvanceOfferLimitData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GetSaLimitResponse extends BaseResponse {
   private SalaryAdvanceOfferLimitData data;
}
