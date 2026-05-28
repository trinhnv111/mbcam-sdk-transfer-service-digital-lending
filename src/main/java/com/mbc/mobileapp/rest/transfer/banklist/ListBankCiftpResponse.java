package com.mbc.mobileapp.rest.transfer.banklist;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpBankInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListBankCiftpResponse extends BaseResponse {
    private List<CiftpBankInfo> lstBank;
}
