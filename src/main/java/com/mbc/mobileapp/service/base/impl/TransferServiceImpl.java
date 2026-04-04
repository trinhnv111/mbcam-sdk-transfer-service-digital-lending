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
import com.mbc.mobileapp.command.transfer.ciftp.wallet.DoCiftpExecuteTransferToWallet;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.transfer.MakeTransferResponse;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import com.mbc.mobileapp.rest.transfer.TransInfoResponse;
import com.mbc.mobileapp.rest.transfer.banklist.ListBankCiftpResponse;
import com.mbc.mobileapp.rest.transfer.ciftp.AccountInquiryResponse;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRCheckInfoResponse;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRMakeTransferResponse;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRTransferResponse;
import com.mbc.mobileapp.service.base.TransferService;
import com.mbc.mobileapp.service.transfer.ciftp.CiftpAccountInquiryCasaService;
import com.mbc.mobileapp.service.transfer.ciftp.CiftpExecuteToCasaService;
import com.mbc.mobileapp.service.transfer.ciftp.CiftpGetListBankService;
import com.mbc.mobileapp.service.transfer.ciftp.CiftpValidToCasaService;
import com.mbc.mobileapp.service.transfer.ciftp.wallet.CiftpAccountInquiryWalletService;
import com.mbc.mobileapp.service.transfer.ciftp.wallet.CiftpExecuteToWalletService;
import com.mbc.mobileapp.service.transfer.ciftp.wallet.CiftpValidateToWalletService;
import com.mbc.mobileapp.service.transfer.inhouse.MakeTransferService;
import com.mbc.mobileapp.service.transfer.inhouse.ValidTransferService;
import com.mbc.mobileapp.service.transfer.khqr.KHQRCheckInfoService;
import com.mbc.mobileapp.service.transfer.khqr.KHQRExecuteTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TransferServiceImpl extends ServiceBase implements TransferService {

    @Autowired
    private ValidTransferService validTransferService;

    @Autowired
    private MakeTransferService makeTransferService;

    @Autowired
    private CiftpGetListBankService ciftpGetListBankService;

    @Autowired
    private CiftpAccountInquiryCasaService ciftpAccountInquiryCasaService;

    @Autowired
    private CiftpValidToCasaService ciftpValidToCasaService;

    @Autowired
    private CiftpExecuteToCasaService ciftpExecuteToCasaService;

    @Autowired
    private CiftpAccountInquiryWalletService ciftpAccountInquiryWalletService;

    @Autowired
    private KHQRCheckInfoService khqrCheckInfoService;

    @Autowired
    private CiftpValidateToWalletService ciftpValidateToWalletService;

    @Autowired
    private CiftpExecuteToWalletService ciftpExecuteToWalletService;

    @Autowired
    private KHQRExecuteTransfer khqrExecuteTransfer;

    @Override
    public TransInfoResponse validateTransfer(Request request, CustInfo cust) {
        ProcessContext processContext = loadContext(request, cust);
        TransInfoResponse response = new TransInfoResponse();

        try {
            String srvcCd = request.getSrvcCd();
            if(Constant.SrvcCd.SRVC_TRANS_INHOUSE.equals(srvcCd)){
                validTransferService.execute(processContext);
            }
            if(Constant.SrvcCd.SRVC_TRANS_CIFTP_CASA.equals(srvcCd)){
                ciftpValidToCasaService.execute(processContext);
            }
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }

        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse serviceResponse = (CommonServiceResponse) processContext.getResponse();
            CommonServiceRequest serviceRequest = (CommonServiceRequest) processContext.getRequest();
            response.setTransId(serviceResponse.getTransId());
            response.setTimestamp(serviceResponse.getTransTime());
            response.setTransInfo(serviceRequest.getTransInfo());

            @SuppressWarnings("unchecked")
            List<EasyPaymentLmtUsed> lstEasyPaymentLmtUsed = (List<EasyPaymentLmtUsed>)
                    processContext.getVar(Constant.KeyVar.EASY_PAYMENT_LIMIT_USED);
            response.setLstEasyPaymentLmtUsed(lstEasyPaymentLmtUsed);

        }

        response.setResult(result);
        return response;
    }

    @Override
    public MakeTransferResponse makeTransfer(Request request, CustInfo cust, TokenOtp otp) {
        ProcessContext processContext = loadContext(request, cust);
        processContext.putVar("otp", otp);

        try {
            String srvcCd = request.getSrvcCd();
            if(Constant.SrvcCd.SRVC_TRANS_INHOUSE.equals(srvcCd)){
                makeTransferService.execute(processContext);
            }
            if(Constant.SrvcCd.SRVC_TRANS_CIFTP_CASA.equals(srvcCd)){
                ciftpExecuteToCasaService.execute(processContext);
            }
            logService.execute(processContext);
        } catch (Exception e) {
            AppLog.error(e);
            processContext.setResult(Validator.Result.UNKNOWN);
        }
        MakeTransferResponse response = new MakeTransferResponse();
        Validator.Result result = processContext.getResult();
        if (result.isOk()) {
            CommonServiceResponse serviceResponse = (CommonServiceResponse) processContext.getResponse();
            response.setTraceCode(serviceResponse.getFt());
            response.setTransHash(serviceResponse.getTransHash());

        }

        if(ResponseCode.PIN_CODE_INCORRECT.getCode().equals(result.getResponseCode())) {
            String count = (String) processContext.getVar(Constant.KeyVar.PIN_CODE_RETRY);
            response.setPincode_incorrect(Objects.nonNull(count) ? Integer.valueOf(count) : 0);
        }

        response.setResult(result);
        return response;
    }

    public ListBankCiftpResponse getListBankCiftp(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        ListBankCiftpResponse response = new ListBankCiftpResponse();
        Validator.Result result = null;
        try {
            ciftpGetListBankService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setLstBank(resp.getLstCiftpBank());

            }
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public AccountInquiryResponse ciftpAccountInquiryCasaService(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        AccountInquiryResponse response = new AccountInquiryResponse();
        Validator.Result result = null;
        try {
            ciftpAccountInquiryCasaService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setAccountInfo(resp.getCiftpAccountInfo());
            }
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public AccountInquiryResponse ciftpAccountInquiryWalletService(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        AccountInquiryResponse response = new AccountInquiryResponse();
        Validator.Result result = null;
        try {
            ciftpAccountInquiryWalletService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setAccountInfo(resp.getCiftpAccountInfo());
            }
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public KHQRCheckInfoResponse checkInfoKHQRTransfer(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        KHQRCheckInfoResponse response = new KHQRCheckInfoResponse();
        Validator.Result result = null;
        try {
            khqrCheckInfoService.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {

                CommonServiceResponse serviceResponse = (CommonServiceResponse) context.getResponse();
                CommonServiceRequest serviceRequest = (CommonServiceRequest) context.getRequest();
                response.setBakongAcctId(serviceResponse.getBakongAcctId());
                if(CommonServiceConstant.BakongQRPayType.REMITTANCE.name().equals(serviceResponse.getQrPayType())){
                    response.setQrPayType(serviceResponse.getQrPayType());
                    response.setTransferType(serviceResponse.getTransferType());
                    response.setCiftpAcountInfo(serviceResponse.getCiftpAccountInfo());
                    response.setInhouseAccountInfo(serviceResponse.getAcctNumberInfo());
                }

                if(CommonServiceConstant.BakongQRPayType.MERCHANT.name().equals(serviceResponse.getQrPayType())){
                    response.setQrPayType(serviceResponse.getQrPayType());
                    response.setTransferType(serviceResponse.getTransferType());
                    response.setCiftpAcountInfo(serviceResponse.getCiftpAccountInfo());
                    response.setMerchantInfo(serviceResponse.getMerchantInfo());
                }

                if(CommonServiceConstant.BakongQRPayType.SOLO.name().equals(serviceResponse.getQrPayType())){
                    response.setQrPayType(serviceResponse.getQrPayType());
                    response.setTransferType(serviceResponse.getTransferType());
                    response.setCiftpAcountInfo(serviceResponse.getCiftpAccountInfo());
                    response.setMerchantInfo(serviceResponse.getMerchantInfo());
                }
            }

        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }

        response.setResult(result);
        return response;
    }

    @Override
    public KHQRTransferResponse validKHQRTransfer(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        KHQRTransferResponse response = new KHQRTransferResponse();
        Validator.Result result = null;
        try {
            CommonServiceRequest rq = (CommonServiceRequest) request;
            TransInfo transInfo = rq.getTransInfo();
            if(CommonServiceConstant.TransferType.INHOUSE.name().equals(transInfo.getTransferType())){
                validTransferService.execute(context);
            }

            if(CommonServiceConstant.TransferType.CIFTP.name().equals(transInfo.getTransferType())){
                ciftpValidateToWalletService.execute(context);
            }

            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {

                CommonServiceResponse serviceResponse = (CommonServiceResponse) context.getResponse();
                CommonServiceRequest serviceRequest = (CommonServiceRequest) context.getRequest();

                response.setTransInfo(serviceRequest.getTransInfo());
                response.setTransId(serviceResponse.getTransId());
                response.setTimestamp(serviceResponse.getTransTime());

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
    public KHQRMakeTransferResponse executeKHQRTransfer(Request request, CustInfo cust, TokenOtp otp) {
        ProcessContext context = loadContext(request, cust);
        context.putVar(Constant.KeyVar.OTP, otp);
        KHQRMakeTransferResponse response = new KHQRMakeTransferResponse();
        Validator.Result result = null;
        try {
            khqrExecuteTransfer.execute(context);
            logService.execute(context);

            result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse resp = (CommonServiceResponse) context.getResponse();
                response.setTraceCode(resp.getFt());
                response.setTransHash(resp.getTransHash());
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

}
