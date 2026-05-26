package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.rest.user.initsdk.CustomerInfo;
import lombok.Data;

@Data

public class DisbursementInformationResponse extends BaseResponse {

   private DisburseInfData data;
}
