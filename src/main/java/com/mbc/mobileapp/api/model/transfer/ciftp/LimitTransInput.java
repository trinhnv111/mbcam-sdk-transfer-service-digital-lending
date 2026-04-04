package com.mbc.mobileapp.api.model.transfer.ciftp;

import com.mbc.mobileapp.constant.CommonServiceConstant.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitTransInput {
    private String currency;
    private String custKycStatus;
    private String accountNumber;
    private String cifT24;
    private Service service;
    private String provider;
    private String channel;
    private String qr;
}
