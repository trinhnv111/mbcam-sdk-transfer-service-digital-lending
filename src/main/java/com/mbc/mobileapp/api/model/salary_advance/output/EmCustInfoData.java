package com.mbc.mobileapp.api.model.salary_advance.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * data.customerInfo  → EmCustomerInfo
 * data.salaryInfo    → EmSalaryInfo
 * data.walletBehaviorInfo → EmWalletBehaviorInfo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmCustInfoData {
    private EmCustomerInfo customerInfo;
    private EmSalaryInfo salaryInfo;
    private EmWalletBehaviorInfo walletBehaviorInfo;
}
