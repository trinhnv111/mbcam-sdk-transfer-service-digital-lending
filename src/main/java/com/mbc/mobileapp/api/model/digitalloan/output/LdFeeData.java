package com.mbc.mobileapp.api.model.digitalloan.output;

import lombok.Data;

@Data
public class LdFeeData {
    private String product;
    private String subProduct;
    private String channel;
    private String partnerCode;
    private String feeType;
    private Double feeValue;
    private Double minFee;
    private Double maxFee;
}
