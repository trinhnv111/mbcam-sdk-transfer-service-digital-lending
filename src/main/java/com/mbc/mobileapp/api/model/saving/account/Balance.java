package com.mbc.mobileapp.api.model.saving.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Balance {
	private String working;
	private String available;	
	private String open;
	private String blocked;
	private String overdraftLimit;
}
