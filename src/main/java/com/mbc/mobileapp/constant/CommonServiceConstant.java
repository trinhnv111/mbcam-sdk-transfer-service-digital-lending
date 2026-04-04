
package com.mbc.mobileapp.constant;

import com.mbc.common.util.Constant;

import java.util.HashMap;
import java.util.Map;


public class CommonServiceConstant extends Constant {

    public static final String CHANNEL_MOBILE_RETAIL = "MOBILE.RETAIL";
    public static final String CHANNEL_SDK_RETAIL = "SDK.RETAIL";

    public static final Map<String, String> CHANNEL_SAVING = new HashMap<String, String>() {
        {
            put("EMONEY", "SDK.RETAIL");
        }
    };

    public static final String ID_CARD_DOC_NAME_T24_ID_CARD = "ID.CARD";

    public static final String ID_CARD_DOC_NAME_T24_PASSPORT = "PASSPORT";

    public static final String GENDER_MALE = "MALE";

    public static final String GENDER_FEMALE = "FEMALE";

    public enum Service {
        CASA_TO_WALLET,
        CASA_TO_CASA,
        CASH_BY_CODE,
        CASH_BY_QR
    }

    public enum AccountType {
        ACCOUNT,
        CARD,
        WALLET
    }

    public class Purpose {
        public static final String TRANSFER_TO_RELATIVE = "Transfer to relative";
        public static final String PAYMENT = "payment";
        public static final String PERSONAL_SAVING = "personal saving";
    }

    public enum Channel {
        APP_MBC,
        MBC_MOBILE,
        APP,
        APP_USER
    }

    public enum TransferType {
        BAKONG,
        INTERNATIONAL,
        TRANSFER,
        CASH_BY_CODE,
        CASH_BY_QR,
        CIFTP,
        INHOUSE
    }

    public enum TransactionType {
        INHOUSE,
        FAST,
        ACBK,
        ACCI
    }

    public enum SubType {
        PHONE, ACCOUNT_ID
    }

    public enum BakongQRPayType {
        SOLO,
        REMITTANCE,
        MERCHANT
    }

    public class Referral {
        public static final String REFERRAL = "Cashback for referral";
        public static final String REGISTRATION = "Cashback for registration";
        public static final String ERR_EXCEPTION = "ERR_EXCEPTION";
        public static final String PROCESSING = "PROCESSING";
    }

    public enum AmountType {
        DEBIT,
        CREDIT
    }

    public enum CiftpChannel {
        RETAIL,
        LARGE_VALUE,
        NCS
    }

    public static class SavingType {

        public static final String SAVING_TYPE_NEW = "NEW";

        public static final String SAVING_TYPE_ADMORE = "ADMORE";

        public static final String SAVING_TYPE_DISBURSE = "DISBURSE";
    }

    public static String getCcyKhqr(String code) {

        if ("116".equals(code)) {
            return Constant.CURRENCY_TYPE_KHR;
        } else if ("840".equals(code)) {
            return Constant.CURRENCY_TYPE_USD;
        } else {
            return null;
        }
    }

}
