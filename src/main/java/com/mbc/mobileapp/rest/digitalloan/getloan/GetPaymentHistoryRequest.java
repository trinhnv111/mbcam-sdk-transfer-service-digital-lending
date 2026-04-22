package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;

@Getter
@Setter
public class GetPaymentHistoryRequest extends BaseRequest {
    @Valid
    private PaymentRequest data;
}
