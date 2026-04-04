package com.mbc.mobileapp.config;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ErrorCode {
    MBC_JWS_REQUIRED("4500", "The MBC-JWS parameter is required"),
    DUPLICATE_MESSAGE("4501", "Duplicate message"),
    CERT_INVALID("4502", "Cert and Key is invalid"),
    SIGNATURE_VERIFY_FAIL("4503", "Signature verification failure"),
    MESSAGE_FORMAT_ERROR("4504", "Message format is invalid"),
    PUBLIC_KEY_DECODE_FAIL("4505", "Public key decode fail"),
    NO_HANDLER_FOUND("4506", "No handler found"),

    SUCCESS("000", "Success"),
    UNAUTHORIZED("101", "Unauthorized"),
    FORBIDDEN("102", "Forbidden"),
    REQUEST_TIMEOUT("002", "Request timeout"),
    HEADER_CONTENT_TYPE_INVALID("203", "Header Content-Type is missing or invalid"),
    INPUT_DATA_INVALID("203", "Input data is invalid"),
    INPUT_HIS_DATE_INVALID("203", "startTransactionDate or endTransactionDate is invalid"),
    INPUT_ACCOUNT_NUMBER_INVALID("203", "Input data is invalid"),
    AMOUNT_INVALID("203", "amount is invalid with currency"),
    RECORD_NOT_FOUND("402", "Record doesn't exist"),
    CERT_ERROR("500", "Cert Error"),
    UNKNOWN_ERROR("500", "Unknown Error"),
    SERVICE_UNAVAILABLE("503", "service unavailable"),
    INPUT_ENCRYPT_INVALID("504", "There is something wrong! Our technical team is working on it"),
    CONNECTION_ERROR("4950", "There is something wrong! Our technical team is working on it")
    ;

    private String soaErrorCode;
    private String soaErrorDesc;

    public String getSoaErrorCode() {
        return soaErrorCode;
    }

    public String getSoaErrorDesc() {
        return soaErrorDesc;
    }
}
