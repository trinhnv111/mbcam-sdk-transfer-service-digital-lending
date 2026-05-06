package com.mbc.mobileapp.api.model.salary_advance.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response từ MS Loan API tính toán hạn mức.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsLoanCalculateLimitResponse {

    private String status;
    private String error;
    private String clientMessageId;
    private String path;
    private String soaErrorCode;
    private String soaErrorDesc;
    private LimitData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LimitData {
        /** Mã CIF */
        private String customerCode;
        /** Hạn mức được tính */
        private BigDecimal limitAmount;
        /** Loại tiền hạn mức */
        private String limitCurrency;
        /** Ngày hiệu lực */
        private String limitValueDate;
        /** Ngày hết hiệu lực */
        private String limitEndDate;
    }
}
