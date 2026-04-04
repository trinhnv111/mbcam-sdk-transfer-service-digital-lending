package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.api.model.account.transaction.history.bakong.GetDetailTransactionHistoryOutput;
import com.mbc.mobileapp.rest.account.AccountNumberInfoResponse;
import com.mbc.mobileapp.rest.account.AcctNumberInfo;
import com.mbc.mobileapp.rest.account.NonSavingAccountResponse;
import com.mbc.mobileapp.rest.account.history.GetTransactionHistoryResponse;
import com.mbc.mobileapp.rest.account.history.TransHistoryInfo;
import com.mbc.mobileapp.rest.account.history.bakong.GetDetailTransactionHistoryRsp;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.service.account.GetAccountNumberInfoService;
import com.mbc.mobileapp.service.account.GetDetailHistoryBakongGWService;
import com.mbc.mobileapp.service.account.GetHistoryTransactionService;
import com.mbc.mobileapp.service.account.GetNonSavingAccountService;
import com.mbc.mobileapp.service.base.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl extends ServiceBase implements AccountService {

    @Autowired
    private GetNonSavingAccountService nonSavingAccountService;

    @Autowired
    private GetAccountNumberInfoService getAccountNumberInfoService;

    @Autowired
    private GetHistoryTransactionService getHistoryTransactionService;

    @Autowired
    private GetDetailHistoryBakongGWService getDetailHistoryBakongGWService;

    @Override
    public NonSavingAccountResponse getNonSavingAccount(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);
        try {
            nonSavingAccountService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        NonSavingAccountResponse response = new NonSavingAccountResponse();
        Validator.Result result = processContext.getResult();
        CommonServiceResponse res = (CommonServiceResponse) processContext.getResponse();

        if (result.isOk()) {
            List<AccountBase> output = res.getLstNonSavingAccount();
            response.setAccountList(output);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public AccountNumberInfoResponse getAccountInfo(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);

        try {
            getAccountNumberInfoService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        AccountNumberInfoResponse response = new AccountNumberInfoResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
//            ExecuteT24Output<CustomerInfoT24> output = resp.getCustomerInfo();
//            CustInfoByAcctNo custInfo = new CustInfoByAcctNo();
//            custInfo.setAccountNo(resp.getAcctNo());
//            custInfo.setBranchInfo(output.getData().getBranchInfo());
//            custInfo.setCustomerName(output.getData().getCustomerName().getFirstName() + " " + output.getData().getCustomerName().getLastName());

            List<AcctNumberInfo> t24AccByCif = (List<AcctNumberInfo>) processContext.getVar("T24_SET_ACC");
            response.setCustomerInfo(resp.getAcctNumberInfo());
            response.setAccountList(t24AccByCif);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public GetTransactionHistoryResponse getTransactionHistory(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);
        try {
            getHistoryTransactionService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        GetTransactionHistoryResponse response = new GetTransactionHistoryResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            List<TransHistoryInfo> historyInfoList = resp.getLstTransHistory();
            response.setData(historyInfoList);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public GetDetailTransactionHistoryRsp getDetailHistoryBakong(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);
        GetDetailTransactionHistoryRsp getDetailTransactionHistoryRsp = new GetDetailTransactionHistoryRsp();
        try {
            getDetailHistoryBakongGWService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }

        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            GetDetailTransactionHistoryOutput responseT24 = resp.getGetDetailTransactionHistoryOutput();
            getDetailTransactionHistoryRsp.setData(responseT24);
        }
        getDetailTransactionHistoryRsp.setResult(result);
        return getDetailTransactionHistoryRsp;
    }

}
