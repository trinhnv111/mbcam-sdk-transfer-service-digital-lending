package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor

public class DoGetSaLimitService implements Command {

    @Autowired
    private final ComTransDtlLmtRepository comTransDtlLmtRepository;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result=Validator.Result.OK;
        CustInfo custInfo = processContext.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        try{
            String hostCif =(String) context.get("hostCifId");

            //call repo com_loan_disburment

            ComTransDtlLmt comTransDtlLmt =comTransDtlLmtRepository.
                    findTopByHostCifIdAndStatusOrderByCreatedAtDesc(hostCif, "COM");

            if(comTransDtlLmt!= null){
                BigDecimal approvedLimit = comTransDtlLmt.getApproveLimit();
                BigDecimal usedLimit = comTransDtlLmt.getUsedLimit();

                context.put("approveLimit",approvedLimit);
                context.put("usedLimit",usedLimit);
            }



        } catch (Exception e) {
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(),false,ResponseCode.TRANSACTION_FAIL.getDesc());

        }

        processContext.setResult(result);
        return  !result.isOk();
    }
}
