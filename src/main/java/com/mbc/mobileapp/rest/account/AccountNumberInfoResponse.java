package com.mbc.mobileapp.rest.account;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccountNumberInfoResponse extends BaseResponse {

	public AcctNumberInfo customerInfo;
	public List<AcctNumberInfo> accountList;
}
