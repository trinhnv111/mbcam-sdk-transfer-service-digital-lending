package com.mbc.mobileapp.rest.digitalloan.disbursement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DisburseInfData {
    private String transId;
    private LoanInformation loanInformation;
    private CustomerInformation customerInformation;
}
