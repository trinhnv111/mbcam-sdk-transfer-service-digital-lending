package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ValidDisbursementResponse extends BaseResponse {

    /** ID của hạn mức (ComTransDtlLmt.id) — FE dùng để gửi vào /disbursement */
    private String transId;

    // ─── Slider config ───────────────────────────────────────────────
    /** Số tiền khả dụng còn lại = approveLimit - usedLimit */
    private BigDecimal availableAmount;

    /** Loại tiền của hạn mức: USD / KHR */
    private String currency;

    /** Ngày hết hạn hạn mức (YYYY-MM-DD) — hiển thị trên UI để KH biết hạn */
    private String limitEndDate;

    /** Số tiền tối thiểu có thể rút (min của slider) */
    private BigDecimal minAmount;

    /** Số tiền tối đa có thể rút (max của slider = availableAmount) */
    private BigDecimal maxAmount;

    // ─── Danh sách tài khoản ─────────────────────────────────────────
    /**
     * Danh sách tài khoản để FE hiển thị Bottom Sheet:
     *  - accountType = "EMONEY"   → ví eMoney (nhóm "eMoney accounts")
     *  - accountType = "MBC"      → tài khoản MBC (nhóm "MBCambodia accounts")
     */
    private List<DisbursementAccountInfo> accountList;

    @Getter
    @Setter
    public static class DisbursementAccountInfo {
        /** Số tài khoản / walletId */
        private String acctId;

        /** Tên tài khoản / tên chủ ví */
        private String acctnName;

        /** Loại tiền */
        private String acctnCurrency;

        /** Số dư hiện tại */
        private String actual;

        /** Số điện thoại liên kết (eMoney) */
        private String phoneNo;

        /** Mã ngân hàng / tổ chức (dùng cho CIFTP) */
        private String participantCode;

        /**
         * Loại tài khoản:
         *  "EMONEY" → ví eMoney linked account
         *  "MBC"    → tài khoản thanh toán MBC
         */
        private String accountType;
    }
}
