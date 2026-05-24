package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Data;

@Data
public class DisbursementInformationResponse extends BaseResponse {
   /** ID của registration (ComTransDtlLoanRegistration) — FE gửi lên /genfile */
   private String transId;
   private DisburseInfData data;
}
