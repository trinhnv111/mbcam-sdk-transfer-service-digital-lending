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

    //Dư nợ còn lại
    private BigDecimal amount;

    //Đơn vị tiền tệ
    private String currency;

    //Ngày giải ngân
    private String valueDate;

    //Ngày kết thúc khoản vay
    private String maturityDate;

    /* mục đích sử dụng*/
    private String useOfLoan;

    //Outstanding balance
    private BigDecimal reimburseAmount;

    // Tổng gốc đã trả
    private BigDecimal totalRepayPrinAmt;

    // Tiền lãi / phí dự tính
    private BigDecimal nextRepayIntAmt;

    private List<PdData> pdList;
}
