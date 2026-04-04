package com.mbc.mobileapp.command.address;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.dto.Province;
import com.mbc.common.repository.ComProvinceRepo;
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
public class DoGetAllProvince implements Command {
    
    @Autowired
    private ComProvinceRepo comProvinceRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        AppLog.info(((ProcessContext) cntxt).getRequest().getReference() + " " + this.getClass().getSimpleName());
        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        Validator.Result result = Validator.Result.OK;
        List<Province> lstProvince = new ArrayList<>();
        try {
            if (Utility.isNull(request.getAddressCode())) {
                lstProvince = comProvinceRepo.getAll();
            }else {
                Province province = comProvinceRepo.getByProvinceCode(request.getAddressCode());
                lstProvince.add(province);
            }
            response.setLstProvince(lstProvince);
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
