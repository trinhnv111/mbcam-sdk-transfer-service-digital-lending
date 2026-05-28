package com.mbc.mobileapp.command.transfer.ciftp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPattern {
    private static String REGEX_ACCOUNT_ID="^[0-9].{10,11}";

    private static String REGEX_PHONE="^(?:(?:0|\\+)\\d{1}|0)[1-9](?:\\d{7,8})$";

    public static boolean validateAccountId(final String accountId) {
        Pattern r = Pattern.compile(REGEX_ACCOUNT_ID);

        // Now create matcher object.
        Matcher m = r.matcher(accountId);
        return m.matches();
    }

    public static boolean isPhoneNumber(final String phone){
        Pattern r = Pattern.compile(REGEX_PHONE);

        // Now create matcher object.
        Matcher m = r.matcher(phone);
        return m.matches();
    }
}
