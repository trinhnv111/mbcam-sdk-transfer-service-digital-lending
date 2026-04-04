package com.mbc.mobileapp.api.model.account.transaction.history;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mbc.common.microservice.base.BaseRequest;
import com.mbc.common.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TransactionHistoryDetailInput extends BaseRequest {
    
    @JsonFormat(pattern = DateUtil.DATE_TIME_SIMPLE_REVERSE_2, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date endPostingDate;

    @JsonFormat(pattern = DateUtil.DATE_TIME_SIMPLE_REVERSE_2, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date endTransactionDate;

    private String search;

    private int sizeResponse;

    @JsonFormat(pattern = DateUtil.DATE_TIME_SIMPLE_REVERSE_2, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date startPostingDate;

    @JsonFormat(pattern = DateUtil.DATE_TIME_SIMPLE_REVERSE_2, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date startTransactionDate;
}
