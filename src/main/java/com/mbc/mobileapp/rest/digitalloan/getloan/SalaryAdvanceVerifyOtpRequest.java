package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryAdvanceVerifyOtpRequest extends BaseRequest {
    private String tempRecordId; // ID bản ghi tạm
    private String otp; // Mã OTP khách hàng nhập
}
