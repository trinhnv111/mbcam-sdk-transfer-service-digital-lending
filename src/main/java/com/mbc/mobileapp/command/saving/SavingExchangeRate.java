package com.mbc.mobileapp.command.saving;

import com.mbc.common.util.Constant;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SavingExchangeRate {

    private static final BigDecimal DEFAULT_EXCHANGE_RATE = new BigDecimal(4050);

    public BigDecimal getTotalUSD(BigDecimal khrAmount, BigDecimal usdAmount) {
        // RateT24 rateT24 = getRateByCurrency(Constant.CURRENCY_TYPE_USD, Constant.CURRENCY_TYPE_KHR);
        BigDecimal khrConvertToUSDAmount = khrAmount.divide(DEFAULT_EXCHANGE_RATE, 2, RoundingMode.HALF_UP);
        return usdAmount.add(khrConvertToUSDAmount).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalKHR(BigDecimal khrAmount, BigDecimal usdAmount) {
        // RateT24 rateT24 = getRateByCurrency(Constant.CURRENCY_TYPE_USD, Constant.CURRENCY_TYPE_KHR);
        BigDecimal usdConvertToKHRAmount = usdAmount.multiply(DEFAULT_EXCHANGE_RATE);
        return khrAmount.add(usdConvertToKHRAmount).setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal exchangeAmountToTargetCcy(BigDecimal sourceAmount, String sourceCcy, String targetCcy) {
        if (Constant.CURRENCY_TYPE_USD.equals(sourceCcy) && Constant.CURRENCY_TYPE_KHR.equals(targetCcy)) {
            return sourceAmount.multiply(DEFAULT_EXCHANGE_RATE).setScale(0, RoundingMode.HALF_UP);
        }
        if (Constant.CURRENCY_TYPE_KHR.equals(sourceCcy) && Constant.CURRENCY_TYPE_USD.equals(targetCcy)) {
            return sourceAmount.divide(DEFAULT_EXCHANGE_RATE, 2, RoundingMode.HALF_UP);
        }
        return sourceAmount.setScale(Constant.CURRENCY_TYPE_USD.equals(sourceCcy) ? 2 : 0, RoundingMode.HALF_UP);
    }
}
