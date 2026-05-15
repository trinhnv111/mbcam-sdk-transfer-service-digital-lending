package com.mbc.mobileapp.api.model.digitalloan.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsLdItem {

    private String ldId;

    private String currency;

    private BigDecimal amount;

    private String valueDate;

    private String maturityDate;

    private String branchCode;

    private List<MsPdItem> pdList;
}
