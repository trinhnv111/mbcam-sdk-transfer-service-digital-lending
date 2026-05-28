package com.mbc.mobileapp.api.model.salary_advance.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustInfoOutput {

    private String idNumber;
    private String englishName;
    private String phoneNumber;
    private String email;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String maritalStatus;

    private String currentCountry;
    private String currentProvince;
    private String currentProvinceName;
    private String currentDistrict;
    private String currentDistrictName;
    private String currentCommune;
    private String currentCommuneName;

    private String residentialCountry;
    private String residentialProvince;
    private String residentialDistrict;
    private String residentialCommune;

    private String placeOfBirth;
    private String employStartDate;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String placeOfBirthCountry;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String placeOfBirthCountryName;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String placeOfBirthProvince;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String placeOfBirthProvinceName;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String placeOfBirthDistrict;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String placeOfBirthDistrictName;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String placeOfBirthCommune;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String placeOfBirthCommuneName;
}
