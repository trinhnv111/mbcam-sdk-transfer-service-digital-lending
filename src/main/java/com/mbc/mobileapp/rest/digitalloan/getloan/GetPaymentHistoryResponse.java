package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.digitalloan.output.PaymentHistoryOutPut;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetPaymentHistoryResponse extends BaseResponse {
    private List<PaymentHistoryOutPut> data;
}
