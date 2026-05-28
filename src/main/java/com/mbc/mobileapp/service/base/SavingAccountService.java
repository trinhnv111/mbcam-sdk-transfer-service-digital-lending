package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.Request;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.account.AccountSavingResponse;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.saving.GetListSavingResponse;
import com.mbc.mobileapp.rest.saving.campaign.CampaignSavingReponse;
import com.mbc.mobileapp.rest.saving.close.DepositCloseResponse;
import com.mbc.mobileapp.rest.saving.close.ValidateDepositClosureResponse;
import com.mbc.mobileapp.rest.saving.cob.CheckCoBResponse;
import com.mbc.mobileapp.rest.saving.interest.InterestResponseV2;
import com.mbc.mobileapp.rest.saving.open.OpenSavingResponse;
import com.mbc.mobileapp.rest.saving.open.ValidateSavingResponse;
import com.mbc.mobileapp.rest.saving.topup.TopUpSavingResponse;
import com.mbc.mobileapp.rest.saving.topup.ValidateTopUpSavingResponse;

public interface SavingAccountService {

    public AccountSavingResponse getSavingAccount(Request request, CustInfo cust);

    public GetListSavingResponse getProductSaving(Request request, CustInfo cust);

    public InterestResponseV2 getInterest(CommonServiceRequest request, CustInfo custInfo);

    public ValidateSavingResponse validate(CommonServiceRequest request, CustInfo custInfo);

    public OpenSavingResponse open(CommonServiceRequest request, CustInfo custInfo, TokenOtp otp);

    public ValidateDepositClosureResponse depositClosureValidate(CommonServiceRequest request, CustInfo custInfo);

    public DepositCloseResponse depositClosure(CommonServiceRequest request, CustInfo custInfo, TokenOtp otp);

    public CheckCoBResponse checkCob(CommonServiceRequest request, CustInfo custInfo);

    public AccountSavingResponse getDetailSavingAccount(Request request, CustInfo cust);

    public CampaignSavingReponse getCampaignSaving(Request request, CustInfo cust);

    public ValidateTopUpSavingResponse validateTopUp(CommonServiceRequest request, CustInfo custInfo);

    public TopUpSavingResponse executeTopUp(CommonServiceRequest request, CustInfo custInfo, TokenOtp otp);

}
