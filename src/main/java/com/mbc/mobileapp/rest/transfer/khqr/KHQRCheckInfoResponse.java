package com.mbc.mobileapp.rest.transfer.khqr;

import com.mbc.common.api.models.merchant.MerchantInfo;
import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpAccountInquiryOutput;
import com.mbc.mobileapp.rest.account.AcctNumberInfo;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KHQRCheckInfoResponse extends BaseResponse {

    private String qrPayType;

    private String transferType;

    private CiftpAccountInquiryOutput ciftpAcountInfo;

    private AcctNumberInfo inhouseAccountInfo;

    private MerchantInfo merchantInfo;

    private String bakongAcctId;
}
