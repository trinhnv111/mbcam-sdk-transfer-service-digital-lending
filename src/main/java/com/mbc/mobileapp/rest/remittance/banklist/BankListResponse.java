package com.mbc.mobileapp.rest.remittance.banklist;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceBankListOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BankListResponse extends BaseResponse {
    private List<RemittanceBankListOutput> data;
}
