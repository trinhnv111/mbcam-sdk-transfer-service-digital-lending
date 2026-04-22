package com.mbc.mobileapp.rest.digitalloan.repayment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepaymentInfo {
    private String t24Ft;
    private String t24VersionId;
}
