package com.mbc.mobileapp.rest.saving.detail;

import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountSavingRequest extends RestRequest {

	private String accountId;
    private String accountTypes;
}
