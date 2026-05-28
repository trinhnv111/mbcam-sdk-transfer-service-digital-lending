package com.mbc.mobileapp.rest.digitalloan.repayment;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentResponse extends BaseResponse {
    private RepaymentInfo data;
}
