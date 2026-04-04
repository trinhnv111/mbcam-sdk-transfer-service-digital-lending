package com.mbc.mobileapp.command.address;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.dto.Ward;
import com.mbc.common.repository.ComWardRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DoGetAllWard implements Command {

    @Autowired
    private ComWardRepo comWardRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        AppLog.info(((ProcessContext) cntxt).getRequest().getReference() + " " + this.getClass().getSimpleName());
        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        Validator.Result result = Validator.Result.OK;
        List<Ward> lstWard = new ArrayList<>();
        try {
            if (Utility.isNull(request.getParentAddressCode())) {
                Ward ward = comWardRepo.findByWardCode(request.getAddressCode());
                lstWard.add(ward);
            }else {
                if(Utility.isNull(request.getAddressCode())) {
                    lstWard = comWardRepo.findByDistrictCode(request.getParentAddressCode());
                }else {
                    Ward ward = comWardRepo.findByWardCodeAndDistrictCode(request.getAddressCode(), request.getParentAddressCode());
                    lstWard.add(ward);
                }
            }
            response.setLstWard(lstWard);
            context.setResponse(response);
        }
        catch (Exception error) {
            AppLog.error(error);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
