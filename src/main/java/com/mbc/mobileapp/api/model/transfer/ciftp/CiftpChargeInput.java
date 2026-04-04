package com.mbc.mobileapp.api.model.transfer.ciftp;

import com.mbc.mobileapp.constant.CommonServiceConstant.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CiftpChargeInput {
    private String amount;
    private String currency;
    private String flow;
    private Service service;
    private String qrPayment;
    private String channel;
    private String paymentTypeCode;
    
}
