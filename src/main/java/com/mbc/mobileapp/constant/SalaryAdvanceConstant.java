package com.mbc.mobileapp.constant;

public class SalaryAdvanceConstant {
    public static final String STEP_CREATE_LOAN = "CREATE_LOAN";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String LOAN_TYPE_SALARY_ADVANCE = "SALARY_ADVANCE";
//    private static final String STEP_INIT = "INIT";
    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 60;
    public static final String MSISDN855 = "855";

    /** Loại giải ngân: khách chọn nhận vào tài khoản MBC (chỉ cần Chặng 2 INHOUSE) */
    public static final String DISBURSEMENT_TYPE_MBC_ACCOUNT  = "MBC_ACCOUNT";

    /** Loại giải ngân: khách chọn nhận vào ví eMoney (cần Chặng 2 INHOUSE + Chặng 3 CIFTP) */
    public static final String DISBURSEMENT_TYPE_EMONEY_WALLET = "EMONEY_WALLET";

    /** transferType gửi lên MS FT cho lệnh INHOUSE (Chặng 2) */
    public static final String TRANSFER_TYPE_INHOUSE = "INHOUSE";

    /** transferType gửi lên MS FT cho lệnh CIFTP/Bakong (Chặng 3) */
    public static final String TRANSFER_TYPE_CIFTP   = "CIFTP";

    /** Số lần retry tối đa khi Chặng 3 bị timeout (BRS bước 44) */
    public static final int CIFTP_MAX_RETRY = 3;

    // ── Context keys (dùng chung DoDisbursement ↔ DoPushEmoneyLoan ↔ ServiceImpl) ──
    public static final String CTX_FT_TRANS_HASH  = "ft_trans_hash";
    public static final String CTX_CIFTP_FT       = "ciftp_ft";
    public static final String CTX_CIFTP_STATUS   = "ciftp_status";
    public static final String CTX_MS_LOAN_RESP   = "ms_loan_response";
    public static final String CTX_EM_WALLET_NO   = "em_wallet_number";
    public static final String CTX_EM_WALLET_NAME = "em_wallet_name";

    // ── CIFTP status values ──
    public static final String CIFTP_SUCCESS        = "SUCCESS";
    public static final String CIFTP_FAIL           = "FAIL";
    public static final String CIFTP_FAIL_NO_WALLET = "FAIL_NO_WALLET";

}

