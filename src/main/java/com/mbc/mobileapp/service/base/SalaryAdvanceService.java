package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetSalaryAdvanceOfferLimitResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceInitResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateResponse;


public interface SalaryAdvanceService {
    GetSalaryAdvanceOfferLimitResponse getSalaryAdvanceOfferLimit(CommonServiceRequest request, CustInfo custInfo);

    SalaryAdvanceInitResponse init(CommonServiceRequest request, CustInfo custInfo);

    SalaryAdvanceCreateResponse create(CommonServiceRequest request, CustInfo custInfo , TokenOtp tokenOtp);
}
