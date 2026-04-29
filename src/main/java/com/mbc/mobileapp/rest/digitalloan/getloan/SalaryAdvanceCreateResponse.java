package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryAdvanceCreateResponse extends BaseResponse {
    private String transId;
    private Double limitAmount;
    private String currency;
}
