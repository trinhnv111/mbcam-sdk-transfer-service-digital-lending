package com.mbc.mobileapp.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum CommonResponseCode {
    AMOUNT_INVALID("GW261", "Amount invalid"),
    DISBURSEMENT_ACCOUNT_IS_INVALID("GW602", "Disbursement account is invalid"),
    DONT_HAVE_LOAN_REPAY("GW604", "You don't have any loans to pay yet"),
    NOT_FOUND_LOAN("GW605", "No loan found"),
    INVALID_DISBURSEMENT_AMOUNT("GW606", "Invalid disbursement amount"),
    LOAN_IS_OVERDUE("GW607", "Your loan is overdue"),
    DATE_FORMAT_INVALID("GW611", "Date format invalid");

    @Getter
    private final String errorCode;
    @Getter
    private final String errorDesc;
}
