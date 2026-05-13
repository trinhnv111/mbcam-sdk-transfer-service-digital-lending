package com.mbc.mobileapp.api.model.salary_advance.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body gửi đến MS Loan API tính toán hạn mức.
 * POST /ms-loan/api/v1/calculate-limit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsLoanOfferLimitRequest {

    /**
     * Mã CIF của KH
     */
    private String customerCode;

    /**
     * Kênh gọi API — "SDK"
     */
    private String channel;

    /**
     * Sản phẩm — "DIGITAL_LOAN"
     */
    private String product;

    /**
     * Sub sản phẩm — "SALARY_ADVANCE"
     */
    private String subProduct;

    /**
     * Đối tác — "EMONEY"
     */
    private String partnerCode;

    /**
     * Loại tiền hạn mức — "USD"
     */
    private String limitCurrency;

    /**
     * Thông tin lương 3 tháng
     */
    private List<SalaryDetail> salaryDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalaryDetail {
        private BigDecimal salaryAmount;
    }
}
