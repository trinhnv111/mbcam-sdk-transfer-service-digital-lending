package com.mbc.mobileapp.api.model.digitalloan.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsLoanGetPdOutput {
    private String customer;
    private String customerName;
    private List<PdData> pdLdList;
}
