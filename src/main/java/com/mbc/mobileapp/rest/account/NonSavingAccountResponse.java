package com.mbc.mobileapp.rest.account;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NonSavingAccountResponse extends BaseResponse {

	private List<AccountBase> accountList;
}
