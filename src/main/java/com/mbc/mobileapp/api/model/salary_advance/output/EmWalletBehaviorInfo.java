package com.mbc.mobileapp.api.model.salary_advance.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Nhóm 3 — walletBehaviorInfo (Dữ liệu hành vi sử dụng ví)
 * Mapping từ response: data.walletBehaviorInfo
 * Có thể null ở phase hiện tại
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmWalletBehaviorInfo {
    private String walletId;
    private Boolean utilityPaymentFlag;
    private BigDecimal utilityPaymentAmount;
    private Boolean telcoTopupFlag;
    private BigDecimal telcoTopupAvg;
    private Boolean gamblingCryptoFlag;
    private String loanRepaymentHistory;
    private String interrupted6MonthsSalaryPayments;
    private Integer walletNumber;
    private Integer topUpWallet;
    private BigDecimal topUpWalletAvg;
    private Integer tranfersInWalletNum;
    private Integer tranfersInWalletFreq;
    private BigDecimal tranfersInWalletAvg;
    private Integer tranfersOutWalletNum;
    private Integer tranfersOutWalletFreq;
    private BigDecimal tranfersOutWalletAvg;
    private BigDecimal avgWalletBalance;
}
