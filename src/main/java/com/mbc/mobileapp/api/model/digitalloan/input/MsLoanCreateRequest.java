package com.mbc.mobileapp.api.model.digitalloan.input;

import lombok.Builder;
import lombok.Data;

/**
 * Request gọi MS Loan API createLoan
 * Tương ứng với API "Create Loan" — SDK BE → MS Loan → T24
 */
@Data
@Builder
public class MsLoanCreateRequest {
    /** Mã CIF của khách hàng trên T24 */
    private String customerCode;
    /** Số tiền giải ngân */
    private String loanAmount;
    /** Loại tiền tệ (USD/KHR) */
    private String loanCurrency;
    /** Ngày đáo hạn khoản vay (yyyy-MM-dd) */
    private String loanDueDate;
    /** Tài khoản trung gian Working Account nhận giải ngân */
    private String disbursementAccount;
    /** Mã docId hợp đồng tiếng Anh (ECM) */
    private String docIdEng;
    /** Kênh gọi */
    private String channel;
    /** Mã sản phẩm */
    private String product;
    /** Mã sản phẩm con */
    private String subProduct;
    /** Mã đối tác */
    private String partnerCode;
}
