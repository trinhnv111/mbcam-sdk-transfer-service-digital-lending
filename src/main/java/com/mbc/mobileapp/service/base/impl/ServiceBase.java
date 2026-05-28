
package com.mbc.mobileapp.service.base.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.TransactionLogService;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;

public class ServiceBase {

    protected final static ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    protected TransactionLogService logService;

    public ProcessContext loadContext(Request request, CustInfo cust) {
        CommonServiceResponse response = new CommonServiceResponse();
        ProcessContext ctx = new ProcessContext();
        ctx.setRequest(request);
        ctx.setCustomer(cust);
        ctx.setResponse(response);

        return ctx;
    }

}
