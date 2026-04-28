package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryAdvanceCreateResponse extends BaseResponse {
    // Trả về cờ thành công sinh OTP và các thông tin cần thiết nếu FE cần
    // Vì phần tính limit nằm ở bước sau (verify-otp) nên API này chỉ xác nhận đã sinh OTP.
}
