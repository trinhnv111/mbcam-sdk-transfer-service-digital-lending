package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.common.rest.bean.EasyPaymentLmtUsed;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.remittance.addr.RemittanceAddressResponse;
import com.mbc.mobileapp.rest.remittance.banklist.BankListResponse;
import com.mbc.mobileapp.rest.remittance.finish.MakeTransferFinishResponse;
import com.mbc.mobileapp.rest.remittance.getaccountname.GetAccountNameResponse;
import com.mbc.mobileapp.rest.remittance.init.MakeTransferInitResponse;
import com.mbc.mobileapp.rest.remittance.promocode.GetPromoCodeResponse;
import com.mbc.mobileapp.service.base.RemittanceService;
import com.mbc.mobileapp.service.remittance.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class RemittanceServiceImpl extends ServiceBase implements RemittanceService {

    @Autowired
    private BankListService bankListService;

    @Autowired
    private GetAccountNameService getAccountNameService;

    @Autowired
    private ValidateTransferInitService validateTransferInitService;

    @Autowired
    private TransferFinishService transferFinishService;

    @Autowired
    private GetAddressVnService getAddressVnService;

    @Autowired
    private GetPromoCodeService getPromoCodeService;

    @Override
    public MakeTransferInitResponse validate(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        MakeTransferInitResponse response = new MakeTransferInitResponse();
        Validator.Result result = null;
        try {
            validateTransferInitService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceRequest req = (CommonServiceRequest) context.getRequest();
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setTransId(resp.getTransId());
                response.setInitMakeTransferInfo(req.getInitMakeTransferInfo());
                response.setTimestamp(resp.getTransTime());

                @SuppressWarnings("unchecked")
                List<EasyPaymentLmtUsed> lstEasyPaymentLmtUsed = (List<EasyPaymentLmtUsed>)
                        context.getVar(Constant.KeyVar.EASY_PAYMENT_LIMIT_USED);
                response.setLstEasyPaymentLmtUsed(lstEasyPaymentLmtUsed);

            }
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        response.setResult(result);
        return response;
    }

    @Override
    public MakeTransferFinishResponse finish(CommonServiceRequest request, CustInfo cust, TokenOtp otp) {
        ProcessContext context = loadContext(request, cust);
        context.putVar(Constant.KeyVar.OTP, otp);
        MakeTransferFinishResponse response = new MakeTransferFinishResponse();
        Validator.Result result = null;
        try {
            transferFinishService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setData(resp.getMakeTransferFinishOutput());
            }

            if(ResponseCode.PIN_CODE_INCORRECT.getCode().equals(result.getResponseCode())) {
                String count = (String) context.getVar(Constant.KeyVar.PIN_CODE_RETRY);
                response.setPincode_incorrect(Objects.nonNull(count) ? Integer.valueOf(count) : 0);
            }

        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        response.setResult(result);
        return response;
    }

    @Override
    public BankListResponse getBankList(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        BankListResponse bankListResponse = new BankListResponse();
        Validator.Result result = null;
        try {
            bankListService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                bankListResponse.setData(resp.getRemittanceBankListOutputList());
            }
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        bankListResponse.setResult(result);
        return bankListResponse;
    }

    @Override
    public GetAccountNameResponse getAccountName(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        GetAccountNameResponse getAccountNameResponse = new GetAccountNameResponse();
        Validator.Result result = null;
        try {
            getAccountNameService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                getAccountNameResponse.setData(resp.getGetAccountNameOutput());
            }
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        getAccountNameResponse.setResult(result);
        return getAccountNameResponse;
    }

    @Override
    public RemittanceAddressResponse getAddressVn(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        RemittanceAddressResponse response = new RemittanceAddressResponse();
        Validator.Result result = null;
        try {
            getAddressVnService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setData(resp.getRemittanceAddressOutput());
            }
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        response.setResult(result);
        return response;
    }

    @Override
    public GetPromoCodeResponse getPromoCode(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        GetPromoCodeResponse response = new GetPromoCodeResponse();
        Validator.Result result = null;
        try {
            getPromoCodeService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setLstPromoCode(resp.getLstPromoCode());
            }
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        response.setResult(result);
        return response;
    }
}
