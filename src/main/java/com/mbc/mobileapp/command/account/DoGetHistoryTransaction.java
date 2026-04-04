
package com.mbc.mobileapp.command.account;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.JointHolder;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.DateCompareUtil;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallHistoryTransactionService;
import com.mbc.mobileapp.api.model.account.transaction.history.Message;
import com.mbc.mobileapp.api.model.account.transaction.history.TransactionHistoryDetailInput;
import com.mbc.mobileapp.rest.account.history.TransHistoryInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class DoGetHistoryTransaction implements Command {

    @Autowired
    private CallHistoryTransactionService callHistoryTransactionService;

    @Autowired
    private ApiCustomer apiCustomer;

    @Override
    public boolean execute(Context cntxt) throws Exception {

        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String custId = customer.getId();

        Message message = new Message();
        message.setAccountId(request.getAcctNo());

        TransactionHistoryDetailInput input = new TransactionHistoryDetailInput();

        input.setEndPostingDate(request.getStartPostingDate());
        input.setStartTransactionDate(request.getStartTransactionDate());
        input.setEndTransactionDate(request.getEndTransactionDate());
        input.setStartPostingDate(request.getStartPostingDate());
        input.setSearch("accountNo:" + request.getAcctNo());
        input.setSizeResponse(request.getSizeResponse());

        ExecuteT24Output<List<TransHistoryInfo>> transactionOutput = null;

        try {

            if (DateCompareUtil.daysDiff(request.getStartTransactionDate(), request.getEndTransactionDate()) > 92) {
                result = new SimpleResult(MBCResponseCode.QUERY_TRANS_MAX_DAYS_90.getDesc(), false,
                        MBCResponseCode.QUERY_TRANS_MAX_DAYS_90.getCode());
            }else if(DateCompareUtil.daysDiff(request.getStartTransactionDate(), request.getEndTransactionDate()) < 0) {
                result = new SimpleResult(MBCResponseCode.QUERY_TRANS_DAYS_INVALID.getDesc(), false,
                        MBCResponseCode.QUERY_TRANS_DAYS_INVALID.getCode());
            }
            else {
                // check debit acct
                NonSavingAcctInput inputMessage = new NonSavingAcctInput();
                inputMessage.setAccountId(request.getAcctNo());

                ExecuteT24Output<List<AccountBase>> nonAccOutput =
                        this.apiCustomer.getCurrentAndSavingAccount(inputMessage, customer.getId(), request.getRequestId());
                if (nonAccOutput != null) {
                    if ("200".equals(nonAccOutput.getStatus())) {
                        if (nonAccOutput.getData().size() > 0) {
                            AccountBase accountBase = nonAccOutput.getData().get(0);

                            boolean view = false;
                            if(accountBase.getCustId().equals(customer.getHostCifId())) {
                                view =true;
                            }else {
                                //check tai khoan join holder
                                if("1".equals(accountBase.getJointAccountType()) || "2".equals(accountBase.getJointAccountType())) {
                                    for (JointHolder holder : accountBase.getJointHolder()) {
                                        if(customer.getHostCifId().equals(holder.getJointHolder())) {
                                            view = true;
                                            break;
                                        }else {
                                            view = false;
                                        }
                                    }
                                }
                            }

                            if (view) {
                                transactionOutput =
                                        callHistoryTransactionService.getTransactionInfo(input, custId, request.getRequestId());
                                if (transactionOutput != null) {
                                    if (Constant.CALL_MICROSERVICE_SUCCESS.equals(transactionOutput.getStatus())) {
                                        List<TransHistoryInfo> lstHistory = transactionOutput.getData();
                                        Collections.sort(lstHistory, new Comparator<TransHistoryInfo>() {
                                            public int compare(TransHistoryInfo o1, TransHistoryInfo o2) {
                                                return o2.getTrxDt().compareTo(o1.getTrxDt());
                                            }
                                        });

                                        context.putVar(Constant.KeyVar.TRANSACTION_HISTORY, lstHistory);
                                        response.setLstTransHistory(lstHistory);
                                    }
                                    else if ("500".equals(transactionOutput.getStatus())
                                            && "410".equals(transactionOutput.getSoaErrorCode())) {
                                        context.putVar(Constant.KeyVar.TRANSACTION_HISTORY,
                                                transactionOutput.getData());
                                        response.setLstTransHistory(transactionOutput.getData());
                                    }
                                    else {
                                        result = new SimpleResult(
                                                transactionOutput.getErrorInfo().getErrorDesc() + " - "
                                                        + transactionOutput.getErrorInfo().getErrorDetail(),
                                                false, transactionOutput.getErrorInfo().getErrorCode());
                                    }
                                }
                            }
                            else {
                                result = new SimpleResult(ResponseCode.DEBIT_ACCOUNT_INCORRECT.getDesc(), false,
                                        ResponseCode.DEBIT_ACCOUNT_INCORRECT.getCode());
                            }
                        }
                        else {
                            result = new SimpleResult(ResponseCode.DEBIT_ACCOUNT_INCORRECT.getDesc(), false,
                                    ResponseCode.DEBIT_ACCOUNT_INCORRECT.getCode());
                        }
                    }
                }
            }

        }
        catch (Exception e) {
            AppLog.error("[Exception Get Transaction History] requestId: "+request.getRequestId()+" desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
