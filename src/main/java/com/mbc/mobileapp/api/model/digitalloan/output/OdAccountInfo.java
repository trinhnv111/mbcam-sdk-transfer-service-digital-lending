package com.mbc.mobileapp.api.model.digitalloan.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdAccountInfo {

    private String accountNumber;
    private String category;
    private String accountTitle;
    private String shortTitle;
    private String currency;
    private String subProduct;
    private String openActualBal;
    private String actualBal;
    private String workingBal;
    private String lockAmount;
    private String fromDate;
    private String odBal;
    private String postingRestrict;
    private String accountOfficer;
    private String campaignCode;
    private String productCode;
}
