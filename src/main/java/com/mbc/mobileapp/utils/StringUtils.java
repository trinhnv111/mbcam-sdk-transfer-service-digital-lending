package com.mbc.mobileapp.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class StringUtils {
    public static final Map<String, Pattern> PATTERN_MAP = new HashMap<>();

    public static String[] split(String s, String regex) {
        return split(s, regex, 0);
    }

    public static String[] split(String s, String regex, int limit) {
        Pattern p = PATTERN_MAP.getOrDefault(regex, Pattern.compile(regex));
        return p.split(s, limit);
    }

}
