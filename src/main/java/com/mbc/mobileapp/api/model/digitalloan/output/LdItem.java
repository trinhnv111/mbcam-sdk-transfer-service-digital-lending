package com.mbc.mobileapp.api.model.digitalloan.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LdItem {

    private String ldId;
    private String branchCode;
    //     tổng số dư nợ
    private BigDecimal amount;

    private String partner;
    private String channel;
    private String currency;
    private String valueDate;
    private String maturityDate;

    //    tổng số tiền giải ngân - Outstanding balance
    private BigDecimal amountHits;

    // principal amount
    private BigDecimal totalRepayPrinAmt;


    private List<PdData> pdList;
}
