package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.common.rest.bean.EasyPaymentLmtUsed;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.account.AccountSavingResponse;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
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
import com.mbc.mobileapp.service.base.SavingAccountService;
import com.mbc.mobileapp.service.saving.*;
import com.mbc.mobileapp.service.saving.close.DepositClosureService;
import com.mbc.mobileapp.service.saving.close.ValidateDepositClosureService;
import com.mbc.mobileapp.service.saving.open.OpenSavingService;
import com.mbc.mobileapp.service.saving.open.ValidateOpenSavingService;
import com.mbc.mobileapp.service.saving.topup.ExecuteTopUpSavingService;
import com.mbc.mobileapp.service.saving.topup.ValidateTopUpSavingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SavingAccountServiceImpl extends ServiceBase implements SavingAccountService {

    @Autowired
    private GetSavingAccountService savingAccountService;

    @Autowired
    private GetProductSavingService getProductSavingService;

    @Autowired
    private GetInterestService getInterestService;

    @Autowired
    private ValidateOpenSavingService validateOpenSavingService;

    @Autowired
    private OpenSavingService openSavingService;

    @Autowired
    private ValidateDepositClosureService validateDepositClosureService;

    @Autowired
    private DepositClosureService depositClosureService;

    @Autowired
    private CheckCoBService checkCobService;

    @Autowired
    private GetDetailSavingAccountService getDetailSavingAccountService;

    @Autowired
    private ValidateTopUpSavingService validateTopUpSavingService;

    @Autowired
    private ExecuteTopUpSavingService executeTopUpSavingService;

    @Override
    public AccountSavingResponse getSavingAccount(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);
        try {
            savingAccountService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        AccountSavingResponse response = new AccountSavingResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            response.setAccountList(resp.getLstSavingAccount());
        }

        response.setResult(result);
        return response;
    }

    @Override
    public GetListSavingResponse getProductSaving(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        GetListSavingResponse response = new GetListSavingResponse();
        Validator.Result result = null;
        try {
            getProductSavingService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setLstProductSaving(resp.getLstProductSavingV2());
            }
        }
        catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        response.setResult(result);
        return response;
    }

    @Override
    public InterestResponseV2 getInterest(CommonServiceRequest request, CustInfo custInfo) {
        ProcessContext processContext = loadContext(request, custInfo);
        try {
            getInterestService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        InterestResponseV2 response = new InterestResponseV2();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            response.setData(resp.getInterestResponseV2());
        }

        response.setResult(result);
        return response;
    }

    @Override
    public ValidateSavingResponse validate(CommonServiceRequest request, CustInfo custInfo) {
        ProcessContext processContext = loadContext(request, custInfo);
        try {
            validateOpenSavingService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        ValidateSavingResponse response = new ValidateSavingResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            CommonServiceRequest req = (CommonServiceRequest) processContext.getRequest();
            response.setTransId(resp.getTransId());
            response.setTimestamp(resp.getTransTime());
            response.setSavingInfo(req.getSavingInfo());

            @SuppressWarnings("unchecked")
            List<EasyPaymentLmtUsed> lstEasyPaymentLmtUsed = (List<EasyPaymentLmtUsed>)
                    processContext.getVar(Constant.KeyVar.EASY_PAYMENT_LIMIT_USED);
            response.setLstEasyPaymentLmtUsed(lstEasyPaymentLmtUsed);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public OpenSavingResponse open(CommonServiceRequest request, CustInfo custInfo, TokenOtp otp) {
        ProcessContext processContext = loadContext(request, custInfo);
        processContext.putVar(Constant.KeyVar.OTP, otp);
        try {
            openSavingService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        OpenSavingResponse response = new OpenSavingResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            response.setData(resp.getOpenSavingOutput());
        }

        if(ResponseCode.PIN_CODE_INCORRECT.getCode().equals(result.getResponseCode())) {
            String count = (String) processContext.getVar(Constant.KeyVar.PIN_CODE_RETRY);
            response.setPincode_incorrect(Objects.nonNull(count) ? Integer.valueOf(count) : 0);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public ValidateDepositClosureResponse depositClosureValidate(CommonServiceRequest request, CustInfo custInfo) {
        ProcessContext processContext = loadContext(request, custInfo);
        try {
            validateDepositClosureService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        ValidateDepositClosureResponse response = new ValidateDepositClosureResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            response.setTransId(resp.getTransId());
        }
        response.setResult(result);
        return response;
    }

    @Override
    public DepositCloseResponse depositClosure(CommonServiceRequest request, CustInfo custInfo, TokenOtp otp) {
        ProcessContext processContext = loadContext(request, custInfo);
        processContext.putVar(Constant.KeyVar.OTP, otp);
        try {
            depositClosureService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        DepositCloseResponse response = new DepositCloseResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            response.setData(resp.getDepositClosureOutput());
        }

        response.setResult(result);
        return response;
    }

    @Override
    public CheckCoBResponse checkCob(CommonServiceRequest request, CustInfo custInfo) {
        ProcessContext processContext = loadContext(request, custInfo);
        try {
            checkCobService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        CheckCoBResponse response = new CheckCoBResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            response.setData(resp.getCheckCoBOutput());
        }
        response.setResult(result);
        return response;
    }

    @Override
    public AccountSavingResponse getDetailSavingAccount(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);
        try {
            getDetailSavingAccountService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        AccountSavingResponse response = new AccountSavingResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            response.setAccountList(resp.getLstSavingAccount());
        }

        response.setResult(result);
        return response;
    }

    @Autowired
    private CampaignService campaignService;

    @Override
    public CampaignSavingReponse getCampaignSaving(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);
        CampaignSavingReponse response = new CampaignSavingReponse();
        Validator.Result result = null;
        try {
            campaignService.execute(processContext);
            logService.execute(processContext);

            result = processContext.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
                response.setLstCampaign(resp.getLstCampaignSaving());

            }
        } catch (Exception e) {
            AppLog.error(e);
            result = Validator.Result.UNKNOWN;

        }

        processContext.setResult(result);
        response.setResult(result);
        return response;
    }

    @Override
    public ValidateTopUpSavingResponse validateTopUp(CommonServiceRequest request, CustInfo custInfo) {
        ProcessContext processContext = loadContext(request, custInfo);
        try {
            validateTopUpSavingService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        ValidateTopUpSavingResponse response = new ValidateTopUpSavingResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            CommonServiceRequest req = (CommonServiceRequest) processContext.getRequest();
            response.setTransId(resp.getTransId());
            response.setTimestamp(resp.getTransTime());
            response.setTopUpSavingInfo(req.getTopUpSavingInfo());
        }

        response.setResult(result);
        return response;
    }

    @Override
    public TopUpSavingResponse executeTopUp(CommonServiceRequest request, CustInfo custInfo, TokenOtp otp) {
        ProcessContext processContext = loadContext(request, custInfo);
        processContext.putVar(Constant.KeyVar.OTP, otp);
        try {
            executeTopUpSavingService.execute(processContext);
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        TopUpSavingResponse response = new TopUpSavingResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse resp = (CommonServiceResponse) processContext.getResponse();
            response.setData(resp.getTopUpSavingDepositOutput());
        }

        if (ResponseCode.PIN_CODE_INCORRECT.getCode().equals(result.getResponseCode())) {
            String count = (String) processContext.getVar(Constant.KeyVar.PIN_CODE_RETRY);
            response.setPincode_incorrect(Objects.nonNull(count) ? Integer.parseInt(count) : 0);
        }

        response.setResult(result);
        return response;
    }
}
