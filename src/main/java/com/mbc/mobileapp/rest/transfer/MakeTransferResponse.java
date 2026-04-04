package com.mbc.mobileapp.rest.transfer;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MakeTransferResponse extends BaseResponse {
	
	public String chargeAmount;
	public String currency;
	public String traceCode;
	public String transHash;
	public String transId;

}
