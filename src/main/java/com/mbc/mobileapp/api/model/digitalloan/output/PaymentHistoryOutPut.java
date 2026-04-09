package com.mbc.mobileapp.api.model.digitalloan.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryOutPut {
    private String ldId;
    private String repayDate;
    private String valueDate;
    private String pricipalAmount;
    private String interestAmount;
    private String penaltyInterest;
    private String penaltySpread;
    private String other;
    private String status;
}
