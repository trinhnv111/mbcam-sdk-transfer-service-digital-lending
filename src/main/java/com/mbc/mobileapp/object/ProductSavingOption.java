package com.mbc.mobileapp.object;

import lombok.*;

import java.math.BigDecimal;


@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSavingOption {
    private String nameEn;
    private String nameKh;
    private String category;
    private String subCategory;
    private String productCode;
    private String subProduct;
    private BigDecimal amtMinDepositUsd;
    private BigDecimal amtMinDepositKhr;
    private BigDecimal amtMinTopupUsd;
    private BigDecimal amtMinTopupKhr;
    private BigDecimal amtMaxNotFullKycUsd;
    private BigDecimal amtMaxNotFullKycKhr;
    private String currencyAvailable;
}
