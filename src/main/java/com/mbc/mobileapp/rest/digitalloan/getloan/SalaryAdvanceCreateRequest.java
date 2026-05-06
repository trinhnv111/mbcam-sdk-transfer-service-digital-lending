package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryAdvanceCreateRequest extends BaseRequest {
    private String transId; // ID giao dịch trả về từ init

    /** OTP token **/
    private TokenOtp tokenOTP;

    private String email;
    private String employmentStartDate; // YYYYMMDD
    private String maritalStatus;
    private String placeOfBirth;
    private String placeOfBirthProvince;
    private String placeOfBirthDistrict;
    private String placeOfBirthWard;

    // Address fields (Tỉnh/Huyện/Xã)
    private String currentAddressProvince;
    private String currentAddressDistrict;
    private String currentAddressWard;
}