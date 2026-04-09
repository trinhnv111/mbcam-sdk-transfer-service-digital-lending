package com.mbc.mobileapp.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    private static String REGEX_ACCOUNT_ID = "^[0-9].{10,11}";

    private static String REGEX_PHONE = "^(?:(?:0|\\+)\\d{1}|0)[1-9](?:\\d{7,8})$";

    public static boolean validateAccountId(final String accountId) {
        Pattern r = Pattern.compile(REGEX_ACCOUNT_ID);

        // Now create matcher object.
        Matcher m = r.matcher(accountId);
        return m.matches();
    }

    public static boolean isPhoneNumber(final String phone) {
        Pattern r = Pattern.compile(REGEX_PHONE);

        // Now create matcher object.
        Matcher m = r.matcher(phone);
        return m.matches();
    }

    public static boolean isValidDate(String input, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setLenient(false); // Strict parsing
        if (StringUtils.isEmpty(input))
            return false;
        try {
            dateFormat.parse(input);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static BigDecimal convertStrToNumber(String numStr) {
        if (StringUtils.isEmpty(numStr)) return BigDecimal.ZERO;
        return new BigDecimal(numStr);
    }

    public static BigDecimal sumBigDecimal(BigDecimal num1, BigDecimal... num2) {
        Optional<BigDecimal> optionalNum1 = Optional.ofNullable(num1);
        BigDecimal sum = optionalNum1.orElse(BigDecimal.ZERO);

        for (BigDecimal num : num2) {
            Optional<BigDecimal> optionalNum = Optional.ofNullable(num);
            sum = sum.add(optionalNum.orElse(BigDecimal.ZERO));
        }
        return sum;
    }

    public static BigDecimal sumBigDecimal2(String num1, String... num2) {
        Optional<BigDecimal> optionalNum1 = Optional.ofNullable(convertStrToNumber(num1));
        BigDecimal sum = optionalNum1.orElse(BigDecimal.ZERO);

        for (String num : num2) {
            Optional<BigDecimal> optionalNum = Optional.ofNullable(convertStrToNumber(num));
            sum = sum.add(optionalNum.orElse(BigDecimal.ZERO));
        }
        return sum;
    }

    public static String formatNumber(BigDecimal num) {
        if (Objects.isNull(num)) return StringUtils.EMPTY;
        return num.stripTrailingZeros().toPlainString();
    }
}
