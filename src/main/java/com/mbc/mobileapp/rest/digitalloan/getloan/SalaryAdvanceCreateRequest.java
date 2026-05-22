package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.constant.MaritalStatus;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class SalaryAdvanceCreateRequest extends BaseRequest {
    private String transId; // ID giao dịch trả về từ init

    /** OTP token **/
    private TokenOtp tokenOTP;

    @Size(max = 50, message = "Email must not exceed 50 characters")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(
            regexp = "^((" +
                    "(0[1-9]|[12]\\d|3[01])/(0[1-9]|1[0-2])/(19|20)\\d{2}" +
                    ")|(" +
                    "(19|20)\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])" +
                    "))$",
            message = "Date must be in format dd/MM/yyyy or yyyy-MM-dd"
    )
    private String employmentStartDate;

    private String maritalStatus;
    private String placeOfBirth;
    private String placeOfBirthProvince;
    private String placeOfBirthDistrict;
    private String placeOfBirthWard;

    // Address fields (Tỉnh/Huyện/Xã)
    private String currentAddressProvince;
    private String currentAddressDistrict;
    private String currentAddressWard;

    @NotNull
    private Boolean disabilities;
}