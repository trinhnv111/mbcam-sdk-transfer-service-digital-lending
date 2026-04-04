package com.mbc.mobileapp.api.model.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mbc.common.api.models.customer.CusOccupation;
import com.mbc.common.api.models.customer.T24UpdateCustomer;
import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingUpdateCusInfo extends T24UpdateCustomer {
    
    @JsonProperty("KH.FULL.NAME")
    private String custFullName;
    
    @JsonProperty("CUST.KYC.STATUS")
    private String kycStatus;
    
    @JsonProperty("EMPLOYMENT.STATUS.LIST")
    private List<CusOccupation> employeeStatusList;
    
    @JsonProperty("DATE.OF.BIRTH")
    private String dob;
    
    @JsonProperty("KH.GENDER")
    private String gender;
    
    @JsonProperty("NAT.ID.ISS.PLC")
    private String placeOfIssue;
    
    @JsonProperty("NAT.ID.ISS.DATE")
    private String issueDate;
    
    @JsonProperty("NAT.ID.EXP.DATE")
    private String dateOfExpire;
    
    @JsonProperty("KH.TOWN")
    private String town;
    
    @JsonProperty("KH.DISTRICT")
    private String district;
    
    @JsonProperty("KH.WARDS")
    private String ward;
    
    @JsonProperty("KH.STREET")
    private String street;
    
    @JsonProperty("MOBILE.NUMBER")
    private String phoneNumber;
        
    @JsonProperty("PHONE.1.LIST")
    private List<CustEmail> listEmail;
    
    @JsonProperty("NATIONALITY")
    private String nationality;

    @JsonProperty("SHORT.NAME.LIST")
    private List<CustShortName> lstCustShortName;
    
    @JsonProperty("NAME.1.LIST")
    private List<CustName> lstCustName;
    
}
