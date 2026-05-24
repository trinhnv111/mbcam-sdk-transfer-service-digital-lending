package com.mbc.mobileapp.api.model.salary_advance.output;

import lombok.Data;

/** Data từ eMoney loan/disbursement response — chứa emLoanId */
@Data
public class EmLoanDisbursementData {
    private String MBCLoanId;
    private String emLoanId;
}
