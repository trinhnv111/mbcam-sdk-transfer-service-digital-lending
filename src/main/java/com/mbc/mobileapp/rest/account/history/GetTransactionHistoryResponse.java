package com.mbc.mobileapp.rest.account.history;


import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetTransactionHistoryResponse extends BaseResponse {
    private List<TransHistoryInfo> data ;
}
