package com.mbc.mobileapp.api.model.digitalloan.output;

import lombok.Data;

/**
 * Response từ eMoney loan/disbursement API (section 5.4)
 */
@Data
public class EmLoanDisbursementOutput {
    /** status: 0 = success */
    private Integer status;
    /** code string ví dụ: MSG_SUCCESS */
    private String code;
    /** message */
    private String message;
    /** Data object */
    private Data data;

    @lombok.Data
    public static class Data {
        /** ID khoản nợ trên MBC */
        private String MBCLoanId;
        /** ID khoản nợ do eMoney sinh ra */
        private String emLoanId;
    }
}
