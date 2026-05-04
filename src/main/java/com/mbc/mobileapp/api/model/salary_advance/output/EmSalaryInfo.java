package com.mbc.mobileapp.api.model.salary_advance.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Nhóm 2 — salaryInfo (Thông tin lương)
 * Mapping từ response: data.salaryInfo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmSalaryInfo {
    private Integer walletAgeDays;
    private Integer kycLevel;                  // 0 = non-eKYC, 1 = fully eKYC
    private Boolean continuousSalary6Months;   // KH lương liên tục 6 tháng?
    private BigDecimal salary3mAvgUSD;
    private BigDecimal salary3mAvgKHR;
    private BigDecimal salary3mMinUSD;
    private BigDecimal salary3mMinKHR;
    private BigDecimal salary3mMaxUSD;
    private BigDecimal salary3mMaxKHR;
    private BigDecimal salaryAmountT1USD;
    private BigDecimal salaryAmountT1KHR;
    private BigDecimal salaryAmountT2USD;
    private BigDecimal salaryAmountT2KHR;
    private BigDecimal salaryAmountT3USD;
    private BigDecimal salaryAmountT3KHR;
    private Integer salaryCountT1;
    private Integer salaryCountT2;
    private Integer salaryCountT3;
}
