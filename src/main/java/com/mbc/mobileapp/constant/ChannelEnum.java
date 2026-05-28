package com.mbc.mobileapp.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ChannelEnum {
    T24("T24", "Bank counter"),
    MOBILE_RETAIL("MOBILE.RETAIL", "My Bank App"),
    SDK_RETAIL("SDK.RETAIL", "SDK Retail"),
    ;
    private final String code;
    private final String label;

    public static String getLabelByCode(String code) {
        return Arrays.stream(ChannelEnum.values())
                .filter(ch -> ch.getCode().equals(code))
                .map(ChannelEnum::getLabel)
                .findFirst()
                .orElse(code);
    }
}
