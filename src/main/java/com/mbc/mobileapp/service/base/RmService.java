package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.Request;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.common.rm.RmResponse;


public interface RmService {

    public RmResponse getRmInfoByCode(Request request, CustInfo cust);

}
