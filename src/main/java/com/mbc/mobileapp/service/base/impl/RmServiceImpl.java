package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.common.rm.RmResponse;
import com.mbc.mobileapp.service.base.RmService;
import com.mbc.mobileapp.service.rm.RmInfoByCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RmServiceImpl extends ServiceBase implements RmService {

    @Autowired
    private RmInfoByCodeService rmInfoByCodeService;

    public RmResponse getRmInfoByCode(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        RmResponse response = new RmResponse();
        try {
            rmInfoByCodeService.execute(context);
            logService.execute(context);

            Validator.Result result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setInfo(resp.getLstRmCodeOutput());
            }
        }
        catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        response.setResult(context.getResult());
        return response;
    }
}
