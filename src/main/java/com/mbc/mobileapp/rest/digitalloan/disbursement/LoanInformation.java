package com.mbc.mobileapp.rest.digitalloan.disbursement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanInformation {
    private String referLoanLimit;
    private String fee;
    private String receivingAmount;
    private String dueDate;

}
