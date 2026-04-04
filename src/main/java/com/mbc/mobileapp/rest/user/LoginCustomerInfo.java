package com.mbc.mobileapp.rest.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mbc.common.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCustomerInfo {

    private String sessionId;

    private String phoneNo;

    private String nm;

    private String userId;

    private String id;

    private String photoStr;

    private String fingerPrint;

    private String viaPhone;

    private String bioId;

    private String imUserStatus;

    private String hostCifId;

    private String idTypNo;
    
    private String idTypType;

    private boolean ekycSuccess;

    private String hashBankId;
    
    private String srvcPcCd;
    
    private String srvcPcCdTmp;
    
    private String custSectorCd;
    
    @JsonFormat(pattern = DateUtil.DATE_WITH_SLASH_REVERSE, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date createdDateUserId;
    
    private String kycStatus;
    
    private String nationalId;

    //onboard date
    private String contactDate;

    //onboard date
    private String visaUploadStatus;

    private String visaUpdateDate;

    private String visaUploadRejectCount;

    private String timestamp;
}