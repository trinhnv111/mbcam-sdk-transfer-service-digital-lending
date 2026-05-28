package com.mbc.mobileapp.rest.account;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AccountNumberInfoRequest extends RestRequest {

	@NotBlank
	@NotNull
	private String accountNo;
	
	private String customerNationalId;
}
