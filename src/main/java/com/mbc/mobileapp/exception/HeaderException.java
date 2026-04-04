package com.mbc.mobileapp.exception;

import com.mbc.mobileapp.config.ErrorCode;
import org.springframework.http.HttpStatus;

public class HeaderException extends RuntimeException {
    private ErrorCode errorCode;
    private HttpStatus httpStatus;

    public HeaderException(HttpStatus httpStatus, ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
