
package com.mbc.mobileapp.api.model.saving.account;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.mbc.common.services.il.nonsavingacct.BranchInfo;
import com.mbc.common.services.il.nonsavingacct.JointHolder;
import com.mbc.common.services.il.nonsavingacct.ProductInfo;
import com.mbc.common.services.il.nonsavingacct.RelationshipManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccountSaving {

    private String customerId;

    private String customerName;

    private String accountNumber;

    private String accountType;

    private String accountCurrency;

    private String accountName;

    private String accountShortName;

    private String mnemonicName;

    private String accountStatus;

    private String interestLiquidAccount;

    private JsonNode postingRestrictList;

    private String openDate;

    private String closeDate;

    private String delegate;

    private String principalAmount;

    private String maturityInstructions;

    //NgÃ y má»Ÿ sá»•
    private String valueDate;

    //NgÃ y Ä‘áº¿n háº¡n
    private String maturityDate;

    private String passBook;
    
    private String channel;

    @JsonProperty("partner")
    @JsonAlias("refPartCode")
    private String partner;

    private List<ProductInfo> productInfo;

    private List<BranchInfo> branchInfo;

    private Balance balance;

    @JsonProperty("rm")
    private List<RelationshipManager> relationshipManager;

    private List<JointHolder> jointholder;

    private Tenor tenor;

    private InterestInfo interestInfo;

    // LÃ£i suáº¥t táº¥t toÃ¡n trÆ°á»›c háº¡n
    private String intRatePreclose;

    //LÃ£i dá»± chi
    private String amtAccr;
    
    //Sá»‘ tiá»�n lÃ£i táº¥t toÃ¡n trÆ°á»›c háº¡n
    private String interestPreclosure;


    //update response for saving fixed deposit
    private String openDepositDate;

    private String taxRate;

    private String taxKey;

    private String allInOneProduct;

    private String requestId;

    private String nominatedAccount;
    
    private String interestRate;
    
    private String dateTime;
}
