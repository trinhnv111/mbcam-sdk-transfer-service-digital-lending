package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.dto.ComBeneficiaryDTO;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.beneficiary.BeneficiaryResponse;
import com.mbc.mobileapp.rest.beneficiary.SaveBeneficiaryResponse;
import com.mbc.mobileapp.service.base.BeneficiaryService;
import com.mbc.mobileapp.service.beneficiary.GetBeneficiaryService;
import com.mbc.mobileapp.service.beneficiary.SaveBeneficiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BeneficiaryServiceImpl extends ServiceBase implements BeneficiaryService {

    @Autowired
    private GetBeneficiaryService getBeneficiaryService;

    @Autowired
    private SaveBeneficiaryService saveBeneficiaryService;

    @Override
    public BeneficiaryResponse getBeneficiaryList(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);
        try {
            getBeneficiaryService.execute(processContext);
            logService.execute(processContext);
        }
        catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        BeneficiaryResponse response = new BeneficiaryResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            List<ComBeneficiaryDTO> beneficiaryDTOList =
                    (List<ComBeneficiaryDTO>) processContext.getVar("beneficiaryDTOList");
            response.setBeneficiaryDTOList(beneficiaryDTOList);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public SaveBeneficiaryResponse saveBeneficiary(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);
        try {
            saveBeneficiaryService.execute(processContext);
            logService.execute(processContext);
        }
        catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        SaveBeneficiaryResponse response = new SaveBeneficiaryResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
        }

        response.setResult(result);
        return response;
    }
}
