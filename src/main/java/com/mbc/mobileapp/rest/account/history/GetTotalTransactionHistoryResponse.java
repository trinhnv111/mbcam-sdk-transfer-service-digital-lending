package com.mbc.mobileapp.rest.account.history;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTotalTransactionHistoryResponse extends BaseResponse {
    private TotalTransactionHistory data;
}
