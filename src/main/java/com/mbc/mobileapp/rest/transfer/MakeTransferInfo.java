package com.mbc.mobileapp.rest.transfer;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MakeTransferInfo {
	
	@NotNull
	@NotBlank
	public String transId;

	private String settlement;
	
}
