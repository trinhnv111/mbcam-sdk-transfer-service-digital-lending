
package com.mbc.mobileapp.command.saving;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;

import com.mbc.mobileapp.api.CallSavingService;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.api.model.saving.account.InterestInfo;
import com.mbc.mobileapp.api.model.saving.account.detail.GetSavingAccountListInput;
import com.mbc.mobileapp.constant.format.FormatNumber;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DoGetDetailSavingAccount implements Command {

    @Autowired
    private CallSavingService callSavingService;

    @Override
    public boolean execute(Context cntxt) throws Exception {

        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String custId = customer.getId();
        String requestId = request.getRequestId();
        String account_no = request.getSourceAccountNumber();
        String cif = customer.getHostCifId(); // "106748";

        try {
            String responseStatus = "";
            GetSavingAccountListInput message = new GetSavingAccountListInput();
            message.setCustomerId(null);
            message.setAccountId(account_no);
            ExecuteT24Output<List<AccountSaving>> esbOutput =
                    callSavingService.getSavingAccountList(message, custId, requestId);

            if (esbOutput != null) {
                responseStatus = esbOutput.getStatus();
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(responseStatus)) {
                    if (Objects.nonNull(esbOutput.getData())) {
//                        List<AccountSaving> accountSavings =
//                            esbOutput.getData().stream().sorted(Comparator.comparing(AccountSaving::getOpenDepositDate))
//                                .collect(Collectors.toList());
                        if (cif.equals(esbOutput.getData().get(0).getCustomerId())) {
                            response.setLstSavingAccount(esbOutput.getData().stream().map(acc -> {
                                if (Objects.nonNull(acc.getInterestInfo()))
                                    acc.getInterestInfo().setApproxNetMonthlyInterest(this.interestMonthlyCalculate(acc.getInterestInfo(),
                                            esbOutput.getData().get(0).getTaxRate(),
                                            esbOutput.getData().get(0).getAccountCurrency()));
                                return acc;
                            }).collect(Collectors.toList()));
                        } else {
                            result = new SimpleResult(ResponseCode.SOURCE_ACCOUNT_IS_INACTIVE.getDesc(), false,
                                    ResponseCode.SOURCE_ACCOUNT_IS_INACTIVE.getCode());
                        }
                    }
                } else {
                    if (Constant.CALL_MICROSERVICE_SUCCESS.equals(esbOutput.getSoaErrorCode())) {
                        response.setLstSavingAccount(new ArrayList<AccountSaving>());
                    } else {
                        result = new SimpleResult(
                                esbOutput.getErrorInfo().getErrorDesc() + " - " + esbOutput.getErrorInfo().getErrorDetail(),
                                false, esbOutput.getErrorInfo().getErrorCode());
                    }
                }
            } else {
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
            }

        } catch (Exception e) {
            AppLog.error("ERROR: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

    private BigDecimal interestMonthlyCalculate(InterestInfo interest, String taxRate, String ccy) {
        //Số tiền lãi dự chi tới cuối kỳ
        BigDecimal amtIntEndCapDate = FormatNumber.convertToAmount(interest.getAmtIntEndCapDate());
        //Số % thuế tính trên số tiền lãi
        BigDecimal taxRatee = FormatNumber.convertToAmount(taxRate);

        int scale = Constant.CURRENCY_TYPE_USD.equals(ccy) ? 2 : 0;
        return amtIntEndCapDate
                .subtract((amtIntEndCapDate.multiply(taxRatee).movePointLeft(2)).setScale(scale, RoundingMode.HALF_UP))
                .setScale(scale, RoundingMode.HALF_UP);
    }

}
