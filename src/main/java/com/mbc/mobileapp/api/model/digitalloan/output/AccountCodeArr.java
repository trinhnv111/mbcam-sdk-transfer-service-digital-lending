package com.mbc.mobileapp.api.model.digitalloan.output;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.mbc.common.rest.bean.RmInfo;
import com.mbc.common.services.il.nonsavingacct.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountCodeArr {

    private String custId;
    private String custName;
    private String custKycStatus;
    private String acctId;
    private String acctnType;
    private String acctnCurrency;
    private String acctnName;
    private String acctnShortName;
    private String mnemonic;
    private String acctnStatus;
    private String interestLiquAcct;
    private JsonNode postingRestrictList;

    private List<ProductInfo> productInfo;
    private BranchInfo branchInfo;
    private Balance balance;

    private String openDate;
    private String closeDate;
    @JsonProperty("rm")
    private List<RelationshipManager> relationshipManager;
    private List<JointHolder> jointholder;
    private JsonNode delegate;
    private String expDate;
    private String interestRate;
    private String recordStatus;
    private String transRef;
    private String limitRef;
    private String jointAccountType;
}