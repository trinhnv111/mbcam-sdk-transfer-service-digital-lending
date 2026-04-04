
package com.mbc.mobileapp.service.base;
import com.mbc.common.bean.Request;
import com.mbc.common.object.CustInfo;

import com.mbc.mobileapp.rest.account.AccountNumberInfoResponse;
import com.mbc.mobileapp.rest.account.NonSavingAccountResponse;
import com.mbc.mobileapp.rest.account.history.GetTransactionHistoryResponse;
import com.mbc.mobileapp.rest.account.history.bakong.GetDetailTransactionHistoryRsp;


public interface AccountService  {

    public NonSavingAccountResponse getNonSavingAccount(Request request, CustInfo cust);

    public AccountNumberInfoResponse getAccountInfo(Request request, CustInfo cust);

    public GetTransactionHistoryResponse getTransactionHistory(Request request, CustInfo cust);

    public GetDetailTransactionHistoryRsp getDetailHistoryBakong(Request request, CustInfo cust);
}
