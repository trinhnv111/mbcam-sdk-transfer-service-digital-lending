package com.mbc.mobileapp.api.model.digitalloan.output;

import lombok.Data;

/**
 * Response từ MS Loan createLoan API
 */
@Data
public class MsLoanCreateOutput {
    /** Mã khoản vay LD từ T24 Core Banking */
    private String ldId;
    /** Tài khoản Working Account nhận giải ngân (drawdownAccount) */
    private String drawdownAccount;
    /** Số tiền thực nhận sau phí */
    private String receivingAmount;
    /** Loại tiền */
    private String currency;
    /** transactionId dùng làm remark cho MakeTransfer */
    private String transactionId;
    /** Ngày hiệu lực */
    private String valueDate;
}
