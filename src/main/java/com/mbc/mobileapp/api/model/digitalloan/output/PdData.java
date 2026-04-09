package com.mbc.mobileapp.api.model.digitalloan.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdData {
    private String pdId;
    private String pdCategory;
    private String prAmt;
    private String inAmt;
    private String peAmt;
    private String psAmt;
    private String subProduct;
    private String startDate;
    private String loanRef;
}
