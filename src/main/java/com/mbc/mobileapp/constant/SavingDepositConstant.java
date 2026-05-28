package com.mbc.mobileapp.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SavingDepositConstant {

    List<String> MATURITY_INSTRUCTION_TYPES = List.of("1", "2", "3");
    Map<String, String> SAVING_ACCOUNT_NAME = new HashMap<String, String>(){
        {
            put("AZ000001", "FIXED_DEPOSIT_ACCOUNT");
            put("AZ000003", "FIXED_DEPOSIT_ACCOUNT");
            put("AZ000002", "REAL_TIME_DEPOSIT_ACCOUNT");
            put("AC000001", "FLEXI_TERM_DEPOSIT_ACCOUNT");

        }
    };

    interface T24KycStatus {
        String FULL_KYC = "FULL.KYC";
        String PARTIAL = "PARTIAL";
        String BASIC = "BASIC";
        String REJECTED = "REJECTED";
    }

    List<String> KYC_STATUS_ALLOW_SAVING_DEPOSIT = Arrays.asList(T24KycStatus.FULL_KYC, T24KycStatus.PARTIAL);

    String REAL_TIME_CONTENT_HEADER_NOTIFY = "Real-time";
    String FLEXI_CONTENT_HEADER_NOTIFY = "Flexi Term";

    interface AccountType {
        String ACCOUNT = "ACCOUNT";
        String CARD = "CARD";
        String WALLET = "WALLET";
    }

    String KHR = "KHR";
    String USD = "USD";

    interface SavingDepositType {

        String INTEREST_PAYMENT_PERIOD = "ENDTERM";

        String CHANNEL = "MOBILE";

        String NAME_ACCOUNT_DEPOSIT="FIXED_DEPOSIT_ACCOUNT";
        String SAVING_TYPE="Fixed Deposit";
        String TIMEOUT="TIMEOUT";
        String KEY_NATIONAL="nationality";
        String DESTINATION_IL="TSA.SERVICE,IL";
        String ACTION_TSA_IL="SEE";
        String VERSION_TSA_IL="1.0";
    }

    interface  DisburseType{
        String CLOSE_AT_MATURITY="1-Close at maturity";
        String PRINCIPAL_AND_INTEREST_ROLLOVER="2-Principal and interest rollover";
        String PRINCIPAL_ROLLOVER="3-Principal rollover";
    }

    interface SavingType {

        String SAVING_TYPE_NEW = "NEW";

        String SAVING_TYPE_ADMORE = "ADMORE";

        String SAVING_TYPE_DISBURSE = "DISBURSE";
    }
}
