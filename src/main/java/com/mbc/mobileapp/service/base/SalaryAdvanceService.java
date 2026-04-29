package com.mbc.mobileapp.service.base;

import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetSaLimitResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceInitResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateResponse;


public interface SalaryAdvanceService {
    SalaryAdvanceInitResponse init(CommonServiceRequest request, CustInfo cust);

    GetSaLimitResponse getSaLimit(CommonServiceRequest request, CustInfo custInfo);

    SalaryAdvanceCreateResponse create(CommonServiceRequest request, CustInfo custInfo);


}
