package com.mbc.mobileapp.api.model.salary_advance.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class EmCustInfoOutput {

    // ---- Nhóm 1: Định danh & Nhân khẩu học ----
    private String customerId;
    private String familyName;
    private String firstName;
    private String englishName;
    private String idType;
    private String idNumber;
    private String idExpiredDate;
    private String gender;
    private String maritalStatus;
    private String nationality;
    private String nationalId;;
    private String dateOfBirth;


    // ---- Nơi sinh ----
    private String placeOfBirthCountry;
    private String placeOfBirthProvince;
    private String placeOfBirthDistrict;
    private String placeOfBirthCommune;

    private String email;

    // ---- Nơi cư trú hiện tại ----
    private String residentialCountry;
    private String residentialProvince;
    private String residentialDistrict;
    private String residentialCommune;
    private String residentialVillage;

    private String phoneNumber;

    // ---- Thông tin việc làm ----
    private String companyName;
    private String currentOccupation;
    private String employmentDate;
    private Integer occupationLengthService;

    // ---- Thông tin lương ----
    private BigDecimal monthlySalaryAmountUsd;
    private Boolean six_months_salary_payments;

    // ---- Nơi làm việc ----
    private String workCountry;
    private String workProvince;
    private String workDistrict;
    private String workCommune;
    private String workVillage;
}

