package com.mbc.mobileapp.api.model.digitalloan.detail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdLdDetailItem {

    /** Mã hợp đồng quá hạn */
    private String pdId;

    /** Sub product */
    private String subProduct;

    /** Số tiền gốc quá hạn */
    private String prAmt;

    /** Số tiền lãi quá hạn */
    private String inAmt;

    /** Số tiền phạt trên gốc quá hạn */
    private String peAmt;

    /** Số tiền phạt trên lãi quá hạn */
    private String psAmt;

    /** Tổng số tiền quá hạn = prAmt + inAmt + peAmt + psAmt */
    private String totalAmtToRepay;
}
