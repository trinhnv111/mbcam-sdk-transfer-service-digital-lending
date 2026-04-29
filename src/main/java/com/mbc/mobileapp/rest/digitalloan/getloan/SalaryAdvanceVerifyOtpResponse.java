package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryAdvanceVerifyOtpResponse extends BaseResponse {
    private Double limit;
    private String currency;
}
