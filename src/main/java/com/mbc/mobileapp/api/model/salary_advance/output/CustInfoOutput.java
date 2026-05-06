package com.mbc.mobileapp.api.model.salary_advance.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thông tin khách hàng trả về trong API init Salary Advance.
 * Chỉ bao gồm các trường thực sự được populate từ mscustomer.
 * Các trường form (email, maritalStatus, address...) FE tự nhập
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustInfoOutput {

    /** Fullname  */
    private String fullName;

    /** ID Number  */
    private String idNumber;

    /** Phone Number */
    private String phoneNumber;
}
