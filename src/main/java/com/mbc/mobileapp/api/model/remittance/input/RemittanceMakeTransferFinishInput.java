package com.mbc.mobileapp.api.model.remittance.input;

import com.mbc.mobileapp.rest.remittance.finish.MakeTransferFinishRequest;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RemittanceMakeTransferFinishInput extends MakeTransferFinishRequest {
    private String partnerCode;

    private String requestMessageID;

    private String feePartner;


    private String feePartnerCurrency;

}
