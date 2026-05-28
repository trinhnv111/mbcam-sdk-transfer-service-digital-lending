package com.mbc.mobileapp.rest.account.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mbc.common.util.DateUtil;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class GetTransactionHistoryRequest extends RestRequest {

    @JsonFormat(pattern = DateUtil.DATE_TIME_SIMPLE_REVERSE_2, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date endPostingDate;

    @JsonFormat(pattern = DateUtil.DATE_TIME_SIMPLE_REVERSE_2, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date endTransactionDate;

    private String accountNo;

    private int sizeResponse;

    @JsonFormat(pattern = DateUtil.DATE_TIME_SIMPLE_REVERSE_2, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date startPostingDate;

    @JsonFormat(pattern = DateUtil.DATE_TIME_SIMPLE_REVERSE_2, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date startTransactionDate;
}
