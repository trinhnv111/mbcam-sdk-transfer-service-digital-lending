package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.digitalloan.output.MsLoanGetPdOutput;
import com.mbc.mobileapp.api.model.digitalloan.output.PdData;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
public class DoValidatePDSalaryAdvance implements Command {

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        
        try {
            MsLoanGetPdOutput pdOutput = response.getPdOutput();
            if (pdOutput != null && !CollectionUtils.isEmpty(pdOutput.getPdLdList())) {
                List<PdData> pdList = pdOutput.getPdLdList();
                for (PdData pd : pdList) {
                    double prAmt = parseAmount(pd.getPrAmt());
                    double inAmt = parseAmount(pd.getInAmt());
                    double peAmt = parseAmount(pd.getPeAmt());
                    double psAmt = parseAmount(pd.getPsAmt());

                    if (prAmt > 0 || inAmt > 0 || peAmt > 0 || psAmt > 0) {
                        log.error("[DoValidatePDSalaryAdvance] Customer has bad debt in PD: {}. prAmt={}, inAmt={}, peAmt={}, psAmt={}", 
                                pd.getPdId(), prAmt, inAmt, peAmt, psAmt);
                        result = new SimpleResult("Customer has bad debt (PD)", false, ResponseCode.TRANSACTION_FAIL.getCode());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("[DoValidatePDSalaryAdvance] Exception: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }

    private double parseAmount(String amtStr) {
        if (Utility.isNull(amtStr)) {
            return 0;
        }
        try {
            return Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
