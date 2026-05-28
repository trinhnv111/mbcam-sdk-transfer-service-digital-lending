package com.mbc.mobileapp.rest.account;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonSavingAccountRequest extends RestRequest {

	private String accountId;
//    private String accountTypes;
}
