package com.mbc.mobileapp.api.model.digitalloan.detail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdDetailItem {

    private OdAccountInfoBrief accountInfo;

    private List<PdOdDetailItem> pdList;


//    public String getAccountNumber() {
//        return accountInfo == null ? null : accountInfo.getAccountNumber();
//    }
}
