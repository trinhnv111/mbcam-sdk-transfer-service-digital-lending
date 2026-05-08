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

import java.util.Optional;

@Slf4j
@Service
public class DoValidatePDSalaryAdvance implements Command {



    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        Validator.Result result = Validator.Result.OK;

        try {
            MsLoanGetPdOutput pdOutput = response.getPdOutput();

            if (pdOutput != null && !CollectionUtils.isEmpty(pdOutput.getPdLdList())) {
                // Tìm kiếm record đầu tiên vi phạm điều kiện PD
                Optional<PdData> badDebtRecord = pdOutput.getPdLdList().stream()
                        .filter(this::hasBadDebt)
                        .findFirst();

                if (badDebtRecord.isPresent()) {
                    PdData pd = badDebtRecord.get();
                    log.error("[DoValidatePDSalaryAdvance] Customer has bad debt in PD: {}. prAmt={}, inAmt={}, peAmt={}",
                            pd.getPdId(), pd.getPrAmt(), pd.getInAmt(), pd.getPeAmt());

                    result = new SimpleResult(ResponseCode.SA_CREDIT_REJECTED.getDesc(), false, ResponseCode.SA_CREDIT_REJECTED.getCode());
                }
            }
        } catch (Exception e) {
            log.error("[DoValidatePDSalaryAdvance] Exception: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);

        return !result.isOk();
    }

    /**
     * check prAmt, inAmt, peAmt
     */
    private boolean hasBadDebt(PdData pd) {
        double prAmt = parseAmount(pd.getPrAmt());
        double inAmt = parseAmount(pd.getInAmt());
        double peAmt = parseAmount(pd.getPeAmt());

        return prAmt > 0 || inAmt > 0 || peAmt > 0;
    }

    private double parseAmount(String amtStr) {
        if (Utility.isNull(amtStr)) {
            return 0;
        }
        try {
            return Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            log.warn("[DoValidatePDSalaryAdvance] Invalid amount format: {}", amtStr);
            return 0;
        }
    }
}