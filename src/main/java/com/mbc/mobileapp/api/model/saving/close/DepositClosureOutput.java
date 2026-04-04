package com.mbc.mobileapp.api.model.saving.close;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mbc.common.util.DateUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Data
public class DepositClosureOutput {
    private String t24VersionId;
    private String requestId;
    private BigDecimal principal;
    private BigDecimal approveAmount;
    private String nominatedAccount;
    private BigDecimal interestAmount;
    //Lãi suất tất toán trước hạn (%)
    private BigDecimal intRatePreClose;
    //Lãi suất tất toán trước hạn
    private BigDecimal intAmtPreClose;
    //Số tiền thuế
    private BigDecimal taxAmount;
    //Lãi đã trả hàng tháng ( đã trừ thuế )
    private BigDecimal intCorr;
    //Lãi nhận được sau khi tất toán
    private BigDecimal redIntAmt;
    //Tổng tiền sẽ nhận được khi tất toán
    private BigDecimal totAmtDue;

    @JsonFormat(pattern = DateUtil.DATE_WITH_SLASH, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date intPostDate;
    private String savingName;
    private String currency;
    private BigDecimal receivingAmount;

    private String categoryInterest;

    public BigDecimal getIntCorr() {
        if (Objects.isNull(intCorr))
            return BigDecimal.ZERO;
        return intCorr;
    }

    public BigDecimal getIntAmtPreClose() {
        if (Objects.isNull(intAmtPreClose))
            return BigDecimal.ZERO;
        return intAmtPreClose;
    }
}
