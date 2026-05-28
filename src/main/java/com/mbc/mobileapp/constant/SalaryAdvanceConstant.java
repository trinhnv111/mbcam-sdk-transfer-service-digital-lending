package com.mbc.mobileapp.constant;

import com.mbc.common.bean.ResponseCode;

import java.util.HashMap;
import java.util.Map;

public class SalaryAdvanceConstant {
    public static final String STEP_CREATE_LOAN = "CREATE_LOAN";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String LOAN_TYPE_SALARY_ADVANCE = "SALARY_ADVANCE";
    //    private static final String STEP_INIT = "INIT";
    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 60;
    public static final String MSISDN855 = "855";

    public static final String CHANNEL = "SDK.RETAIL";
    public static final String PRODUCT = "DIGITAL_LOAN";
    public static final String SUB_PRODUCT = "SALARY_ADVANCE";
    public static final String PARTNER_CODE = "EMONEY";

    // Language codes
    public static final String LANG_KH = "KH";
    public static final String LANG_KHM = "KHM";
    public static final String LANG_KM = "KM";

    // Error messages
    public static final String ERROR_LIMIT_EXPIRED = "Hạn mức ứng lương đã hết hạn. Vui lòng đăng ký lại.";
    public static final String ERROR_LIMIT_EXHAUSTED = "Hạn mức ứng lương đã sử dụng hết.";

    // Country codes & names
    public static final String COUNTRY_CODE_KH = "KH";
    public static final String COUNTRY_NAME_CAMBODIA = "CAMBODIA";


    // eMoney error code mapping
    public static final Map<String, ResponseCode> EMONEY_ERROR_MAP = new HashMap<>();
    static {
        EMONEY_ERROR_MAP.put("MSG_SUCCESS", ResponseCode.SUCCESS);
        EMONEY_ERROR_MAP.put("ERR_MERCHANT_NOT_FOUND", ResponseCode.SRVC_NOT_SUPPORT);
        EMONEY_ERROR_MAP.put("ERR_PARAM_IS_NULL", ResponseCode.INVALID_INPUT);
        EMONEY_ERROR_MAP.put("ERR_PARAM_INVALID", ResponseCode.INVALID_INPUT);
        EMONEY_ERROR_MAP.put("ERR_CUSTOMER_NOT_FOUND", ResponseCode.SA_CREDIT_REJECTED);
        EMONEY_ERROR_MAP.put("ERR_REF_ID_NOT_FOUND", ResponseCode.INVALID_INPUT);
        EMONEY_ERROR_MAP.put("ERR_EMLOANID_NOT_FOUND", ResponseCode.INVALID_INPUT);
        EMONEY_ERROR_MAP.put("ERR_DECRYPT_FAILED", ResponseCode.DYNKEY_DECRYPT_ERROR);
        EMONEY_ERROR_MAP.put("ERR_CUSTOMER_INACTIVE", ResponseCode.SA_CREDIT_REJECTED);
        EMONEY_ERROR_MAP.put("ERR_CUSTOMER_WRONG_INFOR", ResponseCode.SA_ID_MISMATCH);
        EMONEY_ERROR_MAP.put("ERR_DUPLICATE_REF_ID", ResponseCode.DUPLICATE_REF_NO);
        EMONEY_ERROR_MAP.put("ERR_TRANSACTION_TIMEOUT", ResponseCode.REQUEST_TIMEOUT);
        EMONEY_ERROR_MAP.put("ERR_COMMON", ResponseCode.COMMON_FAIL);

        // Custom eMoney errors
        EMONEY_ERROR_MAP.put("ERR_NOT_RECEIVE_SALARY_6M", ResponseCode.SA_CREDIT_REJECTED);
    }


}

