package com.mbc.mobileapp.api.model.salary_advance.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustInfoOutput {

    /** ID bản ghi tạm (ẩn — FE dùng để gửi ngược lại ở API Confirm) */
    private String tempRecordId;

    /** Fullname */
    private String fullName;
    /** ID Number */
    private String idNumber;
    /** Phone Number */
    private String phoneNumber;
    /** Place of Birth (Country dropdown) */
    private String placeOfBirth;
    /** Email */
    private String email;
    /** Employment Start Date */
    private String employStartDate;
    /** Marital Status */
    private String maritalStatus;

    /** Place of Birth Details (Province > District > Commune) */
    private Address placeOfBirthAddress;

    /** Current Address (Province > District > Commune) */
    private Address currentAddress;

    @Data
    public static class Address {
        private String province;
        private String district;
        private String commune;
    }
}
