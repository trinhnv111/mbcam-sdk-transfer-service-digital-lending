package com.mbc.mobileapp.api.model.digitalloan.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsOdItem {


    private String accountNumber;

    private String currency;


    private String odBal;


    private List<MsPdItem> pdList;
}
