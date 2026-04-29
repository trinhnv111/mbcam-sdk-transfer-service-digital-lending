package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryAdvanceCreateRequest extends BaseRequest {
    private String transId; // ID giao dịch trả về từ init
    private String otp; // Mã OTP nhập
    private String email;
    private String employmentStartDate; // YYYYMMDD
    private String maritalStatus;
    private String placeOfBirth;
    
    // Address fields (Tỉnh/Huyện/Xã)
    private String currentAddressProvince;
    private String currentAddressDistrict;
    private String currentAddressWard;
}
