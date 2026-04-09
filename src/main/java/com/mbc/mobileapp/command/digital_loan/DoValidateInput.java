package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.CommonResponseCode;
import com.mbc.mobileapp.constant.ServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.repayment.LoanRepaymentRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.mbc.mobileapp.constant.ServiceConstant.MIN_AMOUNT_REPAY_KHR;
import static com.mbc.mobileapp.constant.ServiceConstant.MIN_AMOUNT_REPAY_USD;

@Service
public class DoValidateInput implements Command {

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        LoanRepaymentRequest repayReq = request.getLoanRepaymentRequest();

        String amount = repayReq.getAmountRepayment();
        String currency = repayReq.getAmountCurrency();

        if (!Utility.isValidAmount(amount) || Double.parseDouble(amount) <= 0
                //validate min amount
                || (ServiceConstant.Currency.KHR.name().equals(currency) && new BigDecimal(amount).compareTo(new BigDecimal(MIN_AMOUNT_REPAY_KHR)) < 0)
                || (ServiceConstant.Currency.USD.name().equals(currency) && new BigDecimal(amount).compareTo(new BigDecimal(MIN_AMOUNT_REPAY_USD)) < 0)) {
            result = new SimpleResult(CommonResponseCode.AMOUNT_INVALID.getErrorDesc(), false,
                                      CommonResponseCode.AMOUNT_INVALID.getErrorCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
