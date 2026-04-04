package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.remittance.addr.RemittanceAddressResponse;
import com.mbc.mobileapp.rest.remittance.banklist.BankListResponse;
import com.mbc.mobileapp.rest.remittance.finish.MakeTransferFinishResponse;
import com.mbc.mobileapp.rest.remittance.getaccountname.GetAccountNameResponse;
import com.mbc.mobileapp.rest.remittance.init.MakeTransferInitResponse;
import com.mbc.mobileapp.rest.remittance.promocode.GetPromoCodeResponse;

public interface RemittanceService {

    public MakeTransferInitResponse validate(CommonServiceRequest request, CustInfo cust);

    public MakeTransferFinishResponse finish(CommonServiceRequest request, CustInfo cust, TokenOtp otp);

    public BankListResponse getBankList(CommonServiceRequest request, CustInfo cust);

    public GetAccountNameResponse getAccountName(CommonServiceRequest request, CustInfo cust);

    public RemittanceAddressResponse getAddressVn(CommonServiceRequest request, CustInfo cust);

    public GetPromoCodeResponse getPromoCode(CommonServiceRequest request, CustInfo cust);
}
