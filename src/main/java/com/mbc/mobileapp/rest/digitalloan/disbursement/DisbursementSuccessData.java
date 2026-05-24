package com.mbc.mobileapp.rest.digitalloan.disbursement;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Data trả về sau giải ngân thành công — hiển thị trên Success screen.
 *
 * Figma fields:
 *   - Disbursement Date
 *   - Due Date
 *   - Loan ID
 *   - Fee
 *   - Receiving Amount
 *   - Disbursement Account
 *   - Transaction Code
 */
@Getter
@Setter
@Builder
public class DisbursementSuccessData {

    /** Mã khoản vay T24 (ldId) */
    private String loanId;

    /** Ngày giải ngân (YYYY-MM-DD) */
    private String disbursementDate;

    /** Ngày đến hạn trả nợ (YYYY-MM-DD) */
    private String dueDate;

    /** Phí xử lý */
    private BigDecimal fee;

    /** Số tiền KH thực nhận = Amount - Fee */
    private BigDecimal receivingAmount;

    /** Tài khoản / ví nhận tiền */
    private String disbursementAccount;

    /**
     * Transaction Code — FT reference (TH1) hoặc transHash Bakong (TH2 eMoney)
     * Figma hiển thị dạng: "02184BCTN20396"
     */
    private String transactionCode;

    /** Loại tiền */
    private String currency;

    /** Số tiền giải ngân */
    private BigDecimal disburseAmount;
}
