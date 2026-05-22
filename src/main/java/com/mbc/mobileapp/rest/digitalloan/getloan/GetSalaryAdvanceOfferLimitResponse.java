package com.mbc.mobileapp.rest.digitalloan.getloan;


import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.salary_advance.output.SalaryAdvanceOfferLimitData;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetSalaryAdvanceOfferLimitResponse extends BaseResponse {
   private SalaryAdvanceOfferLimitData data;
}
