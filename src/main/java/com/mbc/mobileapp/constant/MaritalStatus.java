package com.mbc.mobileapp.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter

public enum MaritalStatus  {
    DIVORCED("Divorced", "D"),
    MARRIED("Married", "M"),
    PARTNER("Partner", "P"),
    SINGLE("Single", "S"),
    WIDOW_WIDOWER("Widow/Widower", "W"),
    SEPARATED("Separated", "SE"),
    OTHER("Other", "O");

    private final String value;
    private final String code;

    MaritalStatus(String value, String code) {
        this.value = value;
        this.code = code;
    }

    @JsonCreator
    public static MaritalStatus from(String input) {
        for (MaritalStatus status : values()) {
            if (status.value.equalsIgnoreCase(input)
                    || status.name().equalsIgnoreCase(input)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid maritalStatus: " + input);
    }


    public static String toCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        for (MaritalStatus status : values()) {
            if (status.value.equalsIgnoreCase(input)
                    || status.name().equalsIgnoreCase(input)) {
                return status.code;
            }
        }
        return input;
    }


    public static String fromCode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        for (MaritalStatus status : values()) {
            if (status.code.equalsIgnoreCase(input)
                    || status.value.equalsIgnoreCase(input)
                    || status.name().equalsIgnoreCase(input)) {
                return status.value;
            }
        }
        return input;
    }


    public static boolean isValid(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        for (MaritalStatus status : values()) {
            if (status.code.equalsIgnoreCase(input)
                    || status.value.equalsIgnoreCase(input)
                    || status.name().equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }
}
