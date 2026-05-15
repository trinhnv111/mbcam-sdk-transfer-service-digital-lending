package com.mbc.mobileapp.api.model.digitalloan.detail;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetLoanDetailRequest extends BaseRequest {

    /** (Optional) Lọc theo LD ID */
    private String ldId;

    /** (Optional) Lọc theo số tài khoản OD */
    private String accountNo;
}
