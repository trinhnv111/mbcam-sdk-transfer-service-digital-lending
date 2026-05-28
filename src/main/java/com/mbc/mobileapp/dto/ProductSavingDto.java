package com.mbc.mobileapp.dto;

import java.math.BigDecimal;

public interface ProductSavingDto {
    String getId();
    String getName();
    String getNameEn();
    String getNameKh();
    String getCategory();
    String getSubCategory();
    String getProductCode();
    String getSubProduct();
    BigDecimal getAmtMinDepositUsd();
    BigDecimal getAmtMinDepositKhr();
    BigDecimal getAmtMinTopupUsd();
    BigDecimal getAmtMinTopupKhr();
    BigDecimal getAmtMaxNotFullKycUsd();
    BigDecimal getAmtMaxNotFullKycKhr();
    String getCurrencyAvailable();
    String getGroupId();
}
