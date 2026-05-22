package com.mbc.mobileapp.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter

public enum MaritalStatus  {
    DIVORCED("Divorced"),
    MARRIED("Married"),
    PARTNER("Partner"),
    SINGLE("Single"),
    WIDOW_WIDOWER("Widow/Widower"),
    SEPARATED("Separated"),
    OTHER("Other");

    private final String value;

    MaritalStatus(String value) {
        this.value = value;
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
}
