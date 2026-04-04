
package com.mbc.mobileapp.api.model.account.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AcctTransactionHistory implements Comparable<AcctTransactionHistory> {

    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @JsonProperty("systemID")
    private String systemID;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("dcType")
    private String dcType;

    @JsonProperty("reversalMarker")
    private String reversalMarker;

    @JsonProperty("postingDate")
    private String postingDate;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("paymentDetail")
    private String paymentDetail;

    @JsonProperty("transactionCode")
    private String transactionCode;

    @JsonProperty("transactionType")
    private String transactionType;

    @JsonProperty("availableBalance")
    private String availableBalance;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("reciprocalAccount")
    private String reciprocalAccount;

    @JsonProperty("reciprocalName")
    private String reciprocalName;

    @JsonProperty("stmtId")
    private String stmtId;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("transactionStatus")
    private String transactionStatus;

    @JsonProperty("bankName")
    private String bankName;

    @JsonProperty("bankCode")
    private String bankCode;

    private String dateTime;

    @Override
    public int compareTo(AcctTransactionHistory o) {
        if (getTransactionDate() == null || o.getTransactionDate() == null)
            return 0;
        return getTransactionDate().compareTo(o.getTransactionDate());
    }
}
