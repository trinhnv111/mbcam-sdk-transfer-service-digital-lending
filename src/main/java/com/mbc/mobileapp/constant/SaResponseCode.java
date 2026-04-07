package com.mbc.mobileapp.constant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum SaResponseCode {
    SA_NO_CREDIT_LIMIT("SA001","No active credit limit found");

    private final String errorCode;
    private final String errorDesc;
}