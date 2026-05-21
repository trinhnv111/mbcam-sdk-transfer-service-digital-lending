package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ValidDisbursementResponse extends BaseResponse {
    private String transId;
    private List<DisbursementAccountInfo> accountList;

    @Getter
    @Setter
    public static class DisbursementAccountInfo {
        private String acctId;
        private String acctnCurrency;
        private String acctnName;
        private String actual;
        private String phoneNo;
    }

}
