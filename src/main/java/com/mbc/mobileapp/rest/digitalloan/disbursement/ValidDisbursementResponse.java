package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ValidDisbursementResponse extends BaseResponse {

    private ValidDisbursementData data;

    @Getter
    @Setter
    public static class ValidDisbursementData {

        public String transId;

        public BigDecimal availableAmount;

        public String currency;

        public String limitEndDate;

        public BigDecimal minAmount;

        public BigDecimal maxAmount;

        public List<DisbursementAccountInfo> accountList;

//        public String isLinkedAccount;
    }

    @Getter
    @Setter
    public static class DisbursementAccountInfo {
        public String acctId;

        public String acctnName;

        public String acctnCurrency;

        public String actual;

        public String phoneNo;

        public String participantCode;

        public String accountType;
    }
}
