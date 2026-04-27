package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetSaLimitResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceInitResponse;

public interface SalaryAdvanceService {
    SalaryAdvanceInitResponse init(CommonServiceRequest request, CustInfo cust);

    GetSaLimitResponse getSaLimit(CommonServiceRequest request, CustInfo custInfo);

    SalaryAdvanceCreateResponse create(CommonServiceRequest request, CustInfo cust, TokenOtp tokenOtp);
}

