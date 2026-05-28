package com.mbc.mobileapp.command.address;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.dto.Country;
import com.mbc.common.repository.ComCountryRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoGetAllCountry implements Command {
    
    @Autowired
    private ComCountryRepo comCountryRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {        
        ProcessContext context = (ProcessContext) cntxt;        
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        Validator.Result result = Validator.Result.OK;        
        try {            
            List<Country> lstCountry = comCountryRepo.getAllCountry();
            response.setLstCountry(lstCountry);
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
