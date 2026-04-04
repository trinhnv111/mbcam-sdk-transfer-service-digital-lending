
package com.mbc.mobileapp.command.saving;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallMsILService;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.api.model.saving.account.SavingAccountListInput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoGetSavingAccount implements Command {

    @Autowired
    private CallMsILService callMsILService;

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
            SavingAccountListInput message = new SavingAccountListInput();
            message.setCustomerId(cif);
            message.setAccountId(account_no);
            ExecuteT24Output<List<AccountSaving>> esbOutput = callMsILService.getSavingAccountListV3(message, custId, requestId);

            if (esbOutput != null) {
                responseStatus = esbOutput.getStatus();
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(responseStatus)) {
                    //Customer doesn't have saving accounts on T24
                    if (CollectionUtils.isEmpty(esbOutput.getData())) {
                        response.setLstSavingAccount(new ArrayList<AccountSaving>());
                        context.setResult(result);
                        return !result.isOk();
                    }
                    List<AccountSaving> accountSavings = esbOutput.getData().stream()
                            .sorted(Comparator.comparing(AccountSaving::getValueDate).reversed())
                            .collect(Collectors.toList());
                    response.setLstSavingAccount(accountSavings);
                } else {
                    result = new SimpleResult(esbOutput.getErrorInfo().getErrorDesc() + " - " + esbOutput.getErrorInfo().getErrorDetail(),
                            false, esbOutput.getErrorInfo().getErrorCode());
                }
            } else {
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
            }
        } catch (Exception e) {
            AppLog.error("[SDK Exception Get Saving Account] requestId: "+requestId+" desc: " , e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

}
