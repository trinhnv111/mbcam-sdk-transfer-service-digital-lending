package com.mbc.mobileapp.constant;

public class ServiceConstant {
    public static final String MIN_AMOUNT_REPAY_USD = "0.1";
    public static final String MIN_AMOUNT_REPAY_KHR = "100";
    public static final String REDIS_KEY_LIST_LOAN = "LIST_LOAN_";

    public enum Currency {
        KHR, USD
    }
}
