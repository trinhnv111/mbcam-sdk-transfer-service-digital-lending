package com.mbc.mobileapp.service.base;

import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.salaryadvance.SaGetLimitInfoResponse;

public interface SalaryAdvanceService {

    SaGetLimitInfoResponse getLimitInfo(CommonServiceRequest request, CustInfo cust);

}
