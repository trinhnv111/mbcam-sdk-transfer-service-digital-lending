package com.mbc.mobileapp.api.model.remittance.output;

import lombok.Data;

@Data
public class RemittanceMakeTransferFinishOutput  {
    private String transactionID;
    private String transactionStatus;
}
