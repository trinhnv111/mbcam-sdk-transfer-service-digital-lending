package com.mbc.mobileapp.rest.account.history.bakong;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetDetailTransactionHistoryRsp extends BaseResponse {
        private Data data;
}
