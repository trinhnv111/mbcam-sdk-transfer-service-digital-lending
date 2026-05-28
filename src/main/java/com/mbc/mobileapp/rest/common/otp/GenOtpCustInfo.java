package com.mbc.mobileapp.rest.common.otp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenOtpCustInfo {

	private String custName;
	private String idCardNumber;
	private String idCardType;
	private String phoneNumber;
}
