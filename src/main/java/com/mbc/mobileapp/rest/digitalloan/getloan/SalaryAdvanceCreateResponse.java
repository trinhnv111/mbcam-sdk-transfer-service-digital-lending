package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryAdvanceCreateResponse extends BaseResponse {
    private String custId;
    private String hostCifId;
    private String customerName;
    private String nationalId;
    private String idType;
    private String phoneNumber;
    private String email;
    private String addressProvince;
    private String addressDistrict;
    private String addressWard;
    private String maritalStatus;
    private Boolean hasUsdAccount;
    private String usdAccountNo;
    private BigDecimal limit;
    private String currency;
}
