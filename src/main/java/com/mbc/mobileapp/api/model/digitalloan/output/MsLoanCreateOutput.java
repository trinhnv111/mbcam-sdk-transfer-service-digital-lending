package com.mbc.mobileapp.api.model.digitalloan.output;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Response từ MS Loan createLoan API
 *
 * Ref: API Spec - Create Loan Output (Data object)
 */
@Data
public class MsLoanCreateOutput {
    /** Mã KH trên T24 (O) */
    private String customerId;
    /** Transaction Id do MS Loan quản lý (O) — dùng làm remark FT */
    private String transactionId;
    /** Mã Limit đã khởi tạo thành công (O) */
    private String limitId;
    /** Mã HĐTD đã khởi tạo thành công (O) */
    private String creditContractId;
    /** Mã LD đã khởi tạo thành công (O) — mã khoản vay T24 */
    private String ldId;
    /** Số tiền vay (O) */
    private String loanAmount;
    /** Kỳ hạn vay (O) */
    private String loanTerm;
    /** Lãi suất khoản vay (O) */
    private String loanInterest;
    /** Số tiền thu phí tạo khoản vay — MS Loan thu ở bước 7 (O) */
    private String loanFee;
    /** TK nhận giải ngân — Working Account (O) */
    private String drawdownAccount;
    /** Tên TK nhận giải ngân (O) */
    private String drawdownAccountName;
    /** Loại tiền TK nhận giải ngân (O) */
    private String drawdownAccountCurrency;
    /** Số tiền thực nhận = loanAmount - loanFee (O) */
    private String actualLoanAmount;
    /** Ngày hiệu lực khoản vay (O) */
    private String valueDate;
    /** Ngày đáo hạn khoản vay (O) */
    private String maturityDate;
}
