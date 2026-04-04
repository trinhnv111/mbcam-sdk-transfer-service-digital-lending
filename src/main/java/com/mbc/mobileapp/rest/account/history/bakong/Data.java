package com.mbc.mobileapp.rest.account.history.bakong;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Data {
    private String transHash;
    private String receiverAcc;
    private String receiverAccCcy;
    private String receiverName;
    private String receiverBankName;
    private String receiverAmount;
    private String receiverBank;
    private String qrPayType;
    private String qrPayment;
    private String bakongService;
}
