package com.mbc.mobileapp.rest.transfer.ciftp;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpAccountInquiryOutput;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountInquiryResponse extends BaseResponse {
        private CiftpAccountInquiryOutput accountInfo;
}
