package com.mbc.mobileapp.object.resgister;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mbc.common.rest.bean.RmInfo;
import com.mbc.common.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class RegisterCustInfo {


    private String username;

    @NotNull
    @NotBlank
    private String pincode;

    private boolean ekycSuccess;

    @NotNull
    @NotBlank
    private String custName;

    private String firstName;

    private String lastName;

    @NotNull
    @NotBlank
    private String idCardNumber;

    @NotNull
    @NotBlank
    private String idCardType;

    @NotNull
    @NotBlank
    private String phoneNumber;

    @JsonFormat(pattern = DateUtil.DATE_WITH_SLASH_REVERSE, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date dateOfIssue;

    @JsonFormat(pattern = DateUtil.DATE_WITH_SLASH_REVERSE, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date dateOfExpire;

    private String placeOfIssue;

    @JsonFormat(pattern = DateUtil.DATE_WITH_SLASH_REVERSE, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date dob;

    @NotNull
    @NotBlank
    private String gender;

    private List<String> currency;

    private RmInfo rmInfo;

    @NotNull
    @NotBlank
    private String occupationId;

    @NotNull
    @NotBlank
    private String occupationTitle;

    private String partnerCode;


    //======ADDRESS================================
    @NotNull
    @NotBlank
    private String address;

    @NotNull
    @NotBlank
    private String provinceCode;

    @NotNull
    @NotBlank
    private String districtCode;

    @NotNull
    @NotBlank
    private String wardCode;

//    @NotNull
//    @NotBlank
    private String street;

    //Quốc tịch
    private String nationalId;

    private String email;


    //======EKYC DATA================================
    //    @NotNull
    //    @NotBlank
    private String bioId;

    private String bioLevel;

    private String deviceId;

    private String hashUserBank;

    private String sessionId;

    private String imagePath;

    private String voicePath;

    private String videoPath;

    private String ekycType;

    //    @NotNull
    //    @NotBlank
    private String kycType;
}
