package com.mbc.mobileapp.rest.account;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccountSavingResponse extends BaseResponse {

	private List<AccountSaving> accountList;
}
