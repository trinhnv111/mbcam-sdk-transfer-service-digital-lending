package com.mbc.mobileapp.exception;

import com.mbc.mobileapp.config.ErrorCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Data
@NoArgsConstructor
public class InputInvalidException extends RuntimeException {

    private int status;
    private String soaErrorCode;
    private String soaErrorDesc;

    public InputInvalidException(String message) {
        super(message);
    }

    public InputInvalidException(String message, ErrorCode errorCode) {
        super(message);
        this.soaErrorCode = errorCode.getSoaErrorCode();
        this.soaErrorDesc = errorCode.getSoaErrorDesc();

        if (StringUtils.isNotEmpty(message))
            this.soaErrorDesc = message;
    }

    public InputInvalidException(ErrorCode errorCode) {
        this.soaErrorCode = errorCode.getSoaErrorCode();
        this.soaErrorDesc = errorCode.getSoaErrorDesc();
    }

    public InputInvalidException(String soaErrorCode, String soaErrorDesc) {
        this.soaErrorCode = soaErrorCode;
        this.soaErrorDesc = soaErrorDesc;
    }

    public InputInvalidException(String soaErrorCode, String soaErrorDesc, int status) {
        this.soaErrorCode = soaErrorCode;
        this.soaErrorDesc = soaErrorDesc;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputInvalidException that = (InputInvalidException) o;
        if(Objects.isNull(that.soaErrorCode) || that.soaErrorCode.isEmpty()){
            return status == that.status;
        }

        if(status == 0){
            return Objects.equals(soaErrorCode, that.soaErrorCode);
        }
        return status == that.status && Objects.equals(soaErrorCode, that.soaErrorCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, soaErrorCode);
    }
}
