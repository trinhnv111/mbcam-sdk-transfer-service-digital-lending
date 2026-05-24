package com.mbc.mobileapp.api.model.digitalloan.input;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request gọi MS Loan API createLoan
 * Tương ứng với API "Create Loan" — SDK BE → MS Loan → T24
 *
 * Ref: API Spec - Create Loan Input
 */
@Data
@Builder
public class MsLoanCreateRequest {

    // ── Thông tin khách hàng ──────────────────────────────────────

    /** Mã CIF của khách hàng trên T24 (M) */
    private String customerCode;
    /** Tên KH (M) */
    private String customerName;
    /** Mã RM quản lý KH (O) */
    private String rmCode;
    /** Số điện thoại (M) */
    private String phoneNumber;
    /** Nghề nghiệp (M) */
    private String occupation;
    /** KH tự làm chủ? Y/N (M) */
    private String selfEmployment;
    /** Ngày bắt đầu làm việc - format: dd/MM/yyyy (M) */
    private String employmentDate;
    /** Tiền lương (M) */
    private BigDecimal monthlySalary;
    /** Loại tiền lương (M) */
    private String salaryCurrency;

    // ── Thông tin khoản vay (loanInfo) ────────────────────────────

    /** Số tiền vay (M) */
    private BigDecimal loanAmount;
    /** Loại tiền vay (M) */
    private String loanCurrency;
    /** Lãi suất khoản vay (M) */
    private BigDecimal loanInterest;
    /** Biên lãi suất (O) */
    private BigDecimal loanInterestSpread;
    /** Ngày hiệu lực khoản vay - format: yyyyMMdd (M) */
    private String valueDate;
    /** Ngày đáo hạn khoản vay - format: yyyyMMdd (M) */
    private String maturityDate;
    /** Kênh: "SDK.RETAIL" (M) */
    private String channel;
    /** Sản phẩm: "DIGITAL_LOAN" (M) */
    private String product;
    /** Sản phẩm con: "SALARY_ADVANCE" (M) */
    private String subProduct;
    /** Mã đối tác: "EMONEY" (M) */
    private String partnerCode;
}
