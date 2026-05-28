package com.mbc.mobileapp.rest.user.initsdk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitSdkInfo {

    private String idCardNumber;

    private String idCardType;

    private String phoneNumber;

    private String partner;

    private String token;

    private String deviceId;

}
