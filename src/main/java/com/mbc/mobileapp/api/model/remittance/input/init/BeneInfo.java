package com.mbc.mobileapp.api.model.remittance.input.init;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BeneInfo {
    private String beneBank;
    private String beneBankName;
    private String beneAccount;
    private String beneDocType;
    private String beneDocNum;
    private String benePhone;
    private String beneName;
    private String beneAddress;
}
