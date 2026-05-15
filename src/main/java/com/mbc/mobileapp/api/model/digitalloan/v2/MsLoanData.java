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
public class MsLoanData {
    private String customer;

    private String customerName;

    private List<MsLdItem> ldList;

    private List<MsOdItem> odList;
}
