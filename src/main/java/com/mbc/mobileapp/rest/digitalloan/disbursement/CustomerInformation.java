package com.mbc.mobileapp.rest.digitalloan.disbursement;

import lombok.Data;

@Data

public class CustomerInformation {
    private String idNumber;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String maritalStatus;
    private String placeOfBrith;
    private String currentAddress;
    private String employmentStartDate;
}
