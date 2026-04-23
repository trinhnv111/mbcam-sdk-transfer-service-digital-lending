package com.mbc.mobileapp.api.model.salary_advance.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body cho API customer/info
 * Field encrypt = RSA(msisdn|idNumber) bằng public key eMoney
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmCustInfoInput {
    private String encrypt;  // RSA encrypted: "msisdn|idNumber"
}
