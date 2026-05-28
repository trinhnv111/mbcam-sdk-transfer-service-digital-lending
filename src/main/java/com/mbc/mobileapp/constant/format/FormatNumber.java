package com.mbc.mobileapp.constant.format;

import com.mbc.common.util.Constant;
import jodd.util.StringUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Objects;

public class FormatNumber {
    public static String formatAmountWithCommasTwoDecimal(String amount) {
        double amount_num = Double.parseDouble(amount);
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(amount_num);
    }

    public static String formatAmount(String amount, String ccy) {
        if (Constant.CURRENCY_TYPE_USD.equals(ccy)) {
            double amount_num = Double.parseDouble(amount);
            DecimalFormat formatter = new DecimalFormat("#,##0.00");
            return formatter.format(amount_num);
        } else {
            double amount_num = Double.parseDouble(amount);
            DecimalFormat formatter = new DecimalFormat("#,###");
            return formatter.format(amount_num);
        }
    }

    public static String formatAmount1(BigDecimal amount, String ccy) {
        if (Objects.isNull(amount)) {
            amount = BigDecimal.ZERO;
        }
        DecimalFormat formatter;
        if (Constant.CURRENCY_TYPE_USD.equals(ccy)) {
            formatter = new DecimalFormat("#,##0.00");
        } else {
            formatter = new DecimalFormat("#,###");
        }
        return formatter.format(amount);
    }

    public static BigDecimal convertToAmount(String amount) {
        if (StringUtil.isEmpty(amount))
            return BigDecimal.ZERO;
        return new BigDecimal(amount);
    }

    public static BigDecimal convertToAmount1(BigDecimal amount) {
        if (Objects.isNull(amount))
            return BigDecimal.ZERO;
        return amount;
    }


}
