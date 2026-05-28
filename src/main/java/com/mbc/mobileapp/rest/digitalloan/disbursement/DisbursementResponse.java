package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisbursementResponse<T> extends BaseResponse {
    private T data;
}
