package com.mbc.mobileapp.api.model.salary_advance.output;

import lombok.Data;

/** Response wrapper từ eMoney /digital-lending/loan/disbursement */
@Data
public class EmLoanDisbursementResponse {
    private Integer status;
    private String code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private String MBCLoanId;
        private String emLoanId;
    }
}
