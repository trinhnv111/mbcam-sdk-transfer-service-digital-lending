package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.Request;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.register.*;

public interface RegisterService {

    public ValidateInfoRegisterResponse validateInfoRegister(Request request, CustInfo cust);

    public GenOTPByPhoneResponse genOtpByPhone(Request request, CustInfo cust);

    public ValidateOTPByPhoneResponse validateOTPByPhone(Request request, TokenOtp otp);

    public ValidateCustInfoResponse validateCustInfo(Request request, CustInfo cust);

    public CreateCustomerResponse createCustomer(Request request, CustInfo cust);

}
