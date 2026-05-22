package com.mbc.mobileapp.api.model.digitalloan.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LdFeeRequest {
    private String channel;
    private String product;
    private String subProduct;
    private String partnerCode;
}