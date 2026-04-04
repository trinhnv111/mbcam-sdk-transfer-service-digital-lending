package com.mbc.mobileapp.command.transfer.khqr;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.CarryService;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.command.account.DoGetAccountNumberInfo;
import com.mbc.mobileapp.command.merchant.DoGetMerchantInfo;
import com.mbc.mobileapp.command.transfer.ciftp.wallet.DoCiftpAccountInquiryWallet;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import kh.org.nbc.bakong_khqr.BakongKHQR;
import kh.org.nbc.bakong_khqr.model.CRCValidation;
import kh.org.nbc.bakong_khqr.model.KHQRDecodeData;
import kh.org.nbc.bakong_khqr.model.KHQRResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DoKHQRCheckInfo implements Command {

    @Autowired
    private DoCiftpAccountInquiryWallet doCiftpAccountInquiryWallet;

    @Autowired
    private DoGetAccountNumberInfo doGetAccountNumberInfo;

    @Autowired
    private DoGetMerchantInfo doGetMerchantInfo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        String qrPayType = "";

        try {
//            KHQRResponse<CRCValidation> khqrResponse = BakongKHQR.verify(request.getPayloadQr());
//            if (!khqrResponse.getData().isValid()) {
//                result = new SimpleResult(MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getDesc(), false,
//                        MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getCode());
//                context.setResult(result);
//                return !result.isOk();
//            }

            KHQRDecodeData dataQR = readPayloadKhqr(request.getPayloadQr());
            if(Objects.nonNull(dataQR)){
                response.setBakongAcctId(dataQR.getBakongAccountID());
                if (dataQR.getMerchantType().equals("29")) {
                    if (!Utility.isNull(dataQR.getAccountInformation())) {
                        qrPayType = CommonServiceConstant.BakongQRPayType.REMITTANCE.name();
                    } else {
                        qrPayType = CommonServiceConstant.BakongQRPayType.SOLO.name();
                    }
                } else if (dataQR.getMerchantType().equals("30")) {
                    qrPayType = CommonServiceConstant.BakongQRPayType.MERCHANT.name();
                    if(dataQR.getMerchantId().startsWith("ATM")){
                        result = new SimpleResult(MBCResponseCode.QR_NOT_SUPPORT.getDesc(), false,
                                MBCResponseCode.QR_NOT_SUPPORT.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }
                } else {
                    qrPayType = "";
                }

                if(CommonServiceConstant.BakongQRPayType.REMITTANCE.name().equals(qrPayType)){
                    if(dataQR.getBakongAccountID().indexOf("@mscb") == -1){
                        response.setTransferType(CommonServiceConstant.TransferType.CIFTP.name());
                        request.setAccountId(dataQR.getBakongAccountID());
                        context.setRequest(request);
                        CarryService carryService = new CarryService(doCiftpAccountInquiryWallet);
                        carryService.execute(context);
                        result = context.getResult();
                    }else{
                        response.setTransferType(CommonServiceConstant.TransferType.INHOUSE.name());
                        request.setAcctNo(dataQR.getAccountInformation());
                        context.setRequest(request);
                        CarryService carryService = new CarryService(doGetAccountNumberInfo);
                        carryService.execute(context);
                        result = context.getResult();
                    }
                }

                if(CommonServiceConstant.BakongQRPayType.MERCHANT.name().equals(qrPayType)){
                    if(dataQR.getBakongAccountID().indexOf("@mscb") == -1){
                        response.setTransferType(CommonServiceConstant.TransferType.CIFTP.name());
                        request.setAccountId(dataQR.getBakongAccountID());
                        context.setRequest(request);
                        CarryService carryService = new CarryService(doCiftpAccountInquiryWallet);
                        carryService.execute(context);
                        result = context.getResult();
                    }else{
                        response.setTransferType(CommonServiceConstant.TransferType.INHOUSE.name());
                        request.setMerchantId(dataQR.getMerchantId());
                        context.setRequest(request);
                        CarryService carryService = new CarryService(doGetMerchantInfo);
                        carryService.execute(context);
                        result = context.getResult();
                    }
                }

                if(CommonServiceConstant.BakongQRPayType.SOLO.name().equals(qrPayType)){
                    response.setTransferType(CommonServiceConstant.TransferType.CIFTP.name());
                    request.setAccountId(dataQR.getBakongAccountID());
                    context.setRequest(request);
                    CarryService carryService = new CarryService(doCiftpAccountInquiryWallet);
                    carryService.execute(context);
                    result = context.getResult();
                }
            }
        }catch (Exception e){
            AppLog.error("[Exception KHQR check info]: requestId: "+ request.getRequestId() + " ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        response.setQrPayType(qrPayType);
        context.setRequest(request);
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

    private KHQRDecodeData readPayloadKhqr(String payload) {
//        String payload = "00020101021229410009khqr@mscb011011111122220210MBCambodia520459995303840540135802KH5915DUONG TRUNG HAI6010PHNOM PENH6214021009062399629917001317261287885856304381D";
        KHQRResponse<KHQRDecodeData> response = BakongKHQR.decode(payload);
        if(response.getKHQRStatus().getCode() == 0) {
            return response.getData();
        }else {
            return null;
        }
    }
}
