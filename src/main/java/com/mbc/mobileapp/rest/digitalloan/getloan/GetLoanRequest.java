package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetLoanRequest extends BaseRequest {
    private String ldId;
    private String accountNo;
}
