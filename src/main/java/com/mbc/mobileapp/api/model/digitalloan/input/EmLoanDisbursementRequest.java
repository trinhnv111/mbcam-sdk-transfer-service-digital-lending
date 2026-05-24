package com.mbc.mobileapp.api.model.digitalloan.input;

import lombok.Builder;
import lombok.Data;

/**
 * Request gọi eMoney API: POST /{merchantCode}/digital-lending/loan/disbursement
 * Ghi nợ vào hệ thống eMoney sau khi Core Banking tạo LD thành công
 */
@Data
@Builder
public class EmLoanDisbursementRequest {
    /** ID khoản nợ trên MBC (= ldId từ MS Loan) */
    private String MBCLoanId;
    /** Mã hóa RSA: msisdn|idNumber */
    private String encrypt;
    /** ID khách hàng trên eMoney (emCustomerId lưu trong COM_LOAN_DISBUR_LMT) */
    private String customerId;
    /** Số tiền giải ngân */
    private String amount;
    /** Loại tiền (KHR/USD) */
    private String currency;
    /** Ngày giải ngân thực tế (yyyy-MM-dd) */
    private String disbursementDate;
    /** Ngày đến hạn trả nợ (yyyy-MM-dd) */
    private String dueDate;
    /** transHash của giao dịch Bakong/CIFTP (TH2 ví eMoney) */
    private String transHash;
    /** Tên công ty của khách hàng */
    private String companyName;
}
