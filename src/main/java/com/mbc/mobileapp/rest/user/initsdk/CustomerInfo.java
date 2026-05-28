package com.mbc.mobileapp.rest.user.initsdk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo {

    private String custName;

    private String idCardNumber;

    private String idCardType;

    private String phoneNumber;

    private String userName;
}
