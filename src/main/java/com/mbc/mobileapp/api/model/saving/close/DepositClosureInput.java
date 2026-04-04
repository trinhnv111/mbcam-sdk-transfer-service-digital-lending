package com.mbc.mobileapp.api.model.saving.close;

import lombok.Data;

@Data
public class DepositClosureInput {
        private String applyCallRate;
        private String id;
        private String nominatedAccount;
        private String preClosureInd;
        private String redemptionAmt;
        private String requestId;
}
