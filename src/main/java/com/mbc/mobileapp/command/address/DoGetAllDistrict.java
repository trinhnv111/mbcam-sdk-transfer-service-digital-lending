package com.mbc.mobileapp.command.address;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.dto.District;
import com.mbc.common.repository.ComDistrictRepo;
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
public class DoGetAllDistrict implements Command{

    @Autowired
    private ComDistrictRepo comDistrictRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        AppLog.info(((ProcessContext) cntxt).getRequest().getReference() + " " + this.getClass().getSimpleName());
        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        Validator.Result result = Validator.Result.OK;
        List<District> lstDistrict = new ArrayList<>();
        try {
            if (Utility.isNull(request.getParentAddressCode())) {
                District district = comDistrictRepo.findByDistrictCode(request.getAddressCode());
                lstDistrict.add(district);
            }else {
                if (Utility.isNull(request.getAddressCode())) {
                    lstDistrict = comDistrictRepo.findByProvinceCode(request.getParentAddressCode());
                }else {
                    District district = comDistrictRepo.findByDistrictCodeAndProvinceCode(request.getAddressCode(), 
                        request.getParentAddressCode());
                    lstDistrict.add(district);
                }
            }
            response.setLstDistrict(lstDistrict);
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
