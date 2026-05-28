package com.mbc.mobileapp.command.transfer.ciftp.wallet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.*;
import com.mbc.common.repository.*;
import com.mbc.common.util.*;
import com.mbc.mobileapp.api.CallFundsTransferService;
import com.mbc.mobileapp.api.model.transfer.ciftp.*;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.api.CallILT24Service;
import com.mbc.common.api.models.exchangerate.RateCurrencyInput;
import com.mbc.common.api.models.exchangerate.RateCurrencyT24;
import com.mbc.common.api.models.exchangerate.RateT24;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.validator.base.Validator;
import com.mbc.common.validator.base.Validator.Result;
import com.mbc.gateway.validator.result.SimpleResult;

import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;

import kh.org.nbc.bakong_khqr.BakongKHQR;
import kh.org.nbc.bakong_khqr.model.CRCValidation;
import kh.org.nbc.bakong_khqr.model.KHQRDecodeData;
import kh.org.nbc.bakong_khqr.model.KHQRResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class DoCiftpValidTransferToWallet implements Command {

    private final List<String> CHECK_SRVC = Arrays.asList("SRVC_TRANS_CIFTP_WALLET", "SRVC_TRANS_CIFTP_KHQR");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CallILT24Service callILT24Service;

    @Autowired
    private ComTransRepo comTransRepo;

    @Autowired
    private ComTransProcessRepo comTransProcessRepo;

    @Autowired
    private ComTransDtlTransferRepo comTransDtlTransferRepo;

    @Autowired
    private ComTransDomesticChargeRepo comTransDomesticChargeRepo;

    @Autowired
    private CallFundsTransferService callFundsTransferService;

    @Value("${provider.domestic.transfer.ciftp}")
    private String provider;

    @Autowired
    private ComFreeTrxVipAcctRepo comFreeTrxVipAcctRepo;

    @Autowired
    private ComServiceFreeVipAcctRepo comServiceFreeVipAcctRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CustInfo customer = context.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        TransInfo khqrTransInfo = request.getTransInfo();
        CiftpAccountInquiryOutput accountInquiryOutput = response.getCiftpAccountInfo();

        String chargeCode = null;
        CiftpChargeOutput ciftpChargeOutput = null;
        RateT24 exchangeRate = null;

        BigDecimal totalDebitAmount = BigDecimal.ZERO;
        LimitTransOutput limitTransOutput = null;
        String inputCcy = null;
        String is_qr = "NO";

        BigDecimal debitChargeAmount = null;
        BigDecimal discountChargeAmount = null;
        String campaignCode = null;

        try {

            if(CHECK_SRVC.indexOf(request.getSrvcCd()) == -1) {
                result = new SimpleResult(ResponseCode.SRVC_INCORRECT.getDesc(), false, ResponseCode.SRVC_INCORRECT.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if (!Utility.isValidAmount(khqrTransInfo.getAmount())) {
                result = new SimpleResult(MBCResponseCode.AMOUNT_INVALID.getDesc(), false,
                        MBCResponseCode.AMOUNT_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if(Constant.SrvcCd.SRVC_TRANS_CIFTP_KHQR.equals(request.getSrvcCd())) {
                if (!Utility.isNull(khqrTransInfo.getPayloadQr())) {
                    is_qr = "YES";
//                    KHQRResponse<CRCValidation> khqrResponse = BakongKHQR.verify(khqrTransInfo.getPayloadQr());
//                    if (!khqrResponse.getData().isValid()) {
//                        result = new SimpleResult(MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getDesc(), false,
//                                MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getCode());
//                        context.setResult(result);
//                        return !result.isOk();
//                    }

                    KHQRDecodeData dataQR = readPayloadKhqr(khqrTransInfo.getPayloadQr());
                    //Check ccy transaction in payload
                    if(!Utility.isNull(dataQR.getTransactionAmount())) {
                        String ccyKhqr = CommonServiceConstant.getCcyKhqr(dataQR.getTransactionCurrency());
                        if(!khqrTransInfo.getCurrency().equals(ccyKhqr)) {
                            result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_KHQR.getDesc(), false,
                                    MBCResponseCode.CIFTP_VALID_INFO_KHQR.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }
                    }

                    //Check info QR
//                    if(khqrTransInfo.getBakongAcctId().equals(dataQR.getBakongAccountID())) {
//
//                        if(!bakongTransInfo.getCreditAcctNo().equals(dataQR.getBakongAccountID())){
//                            result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_KHQR.getDesc(), false,
//                                    MBCResponseCode.CIFTP_VALID_INFO_KHQR.getErrorCode());
//                            context.setResult(result);
//                            return !result.isOk();
//                        }
//
//                        if(ServiceConstant.BakongQRPayType.REMITTANCE.name().equals(bakongTransInfo.getQrPayType())){
//
//                            if(!bakongTransInfo.getCreditAcctName().equals(accountInquiryOutput.getAccountName())){
//                                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_KHQR.getDesc(), false,
//                                        MBCResponseCode.CIFTP_VALID_INFO_KHQR.getErrorCode());
//                                context.setResult(result);
//                                return !result.isOk();
//                            }
//                        }
//
//                        if(ServiceConstant.BakongQRPayType.MERCHANT.name().equals(bakongTransInfo.getQrPayType())){
//                            if(!bakongTransInfo.getCreditAcctName().equals(dataQR.getAcquiringBank())){
//                                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_KHQR.getDesc(), false,
//                                        MBCResponseCode.CIFTP_VALID_INFO_KHQR.getErrorCode());
//                                context.setResult(result);
//                                return !result.isOk();
//                            }
//                        }
//
//                        if(!Utility.isNull(bakongTransInfo.getDestBankName())){
//
//                            if(!bakongTransInfo.getDestBankName().equals(accountInquiryOutput.getBankName())){
//                                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_KHQR.getDesc(), false,
//                                        MBCResponseCode.CIFTP_VALID_INFO_KHQR.getErrorCode());
//                                context.setResult(result);
//                                return !result.isOk();
//                            }
//                        }
//
//                    }else {
//                        result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_KHQR.getDesc(), false,
//                                MBCResponseCode.CIFTP_VALID_INFO_KHQR.getErrorCode());
//                        context.setResult(result);
//                        return !result.isOk();
//                    }

                }else {
                    result = new SimpleResult(MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getDesc(), false,
                            MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
            }else {
                khqrTransInfo.setPayloadQr(null);
            }


//            if(Constant.SrvcCd.SRVC_TRANS_CIFTP_WALLET.equals(request.getSrvcCd())) {

//                if(Constant.CUSTOMER_KYC_STATUS_FULL.equals(response.getCiftpAccountInfo().getKycStatus()) ) {
//                    if(!response.getCiftpAccountInfo().getAccountName().equals(bakongTransInfo.getCreditAcctName())) {
//                        result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_BENE.getDesc(), false,
//                            MBCResponseCode.CIFTP_VALID_INFO_BENE.getErrorCode());
//                        context.setResult(result);
//                        return !result.isOk();
//                    }
//                }

//            }

            if(!response.getCiftpAccountInfo().getBankName().equals(khqrTransInfo.getDestBankName())) {
                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_BENE.getDesc(), false,
                        MBCResponseCode.CIFTP_VALID_INFO_BENE.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if(!response.getCiftpAccountInfo().getBankParticipantCode().equals(khqrTransInfo.getDestBankPartiCode())) {
                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_BENE.getDesc(), false,
                        MBCResponseCode.CIFTP_VALID_INFO_BENE.getCode());
                context.setResult(result);
                return !result.isOk();
            }

//                if(!response.getCiftpAccountInfo().getBankCode().equals(bakongTransInfo.getDestBankCode())) {
//                    result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_BENE.getDesc(), false,
//                        MBCResponseCode.CIFTP_VALID_INFO_BENE.getErrorCode());
//                    context.setResult(result);
//                    return !result.isOk();
//                }

            AccountBase accountInfo = (AccountBase) context.getVar(Constant.KeyVar.DEBIT_ACCOUNT_INFO);
            response.setCurrency(accountInfo.getAcctnCurrency());

            BigDecimal chargeAmount = BigDecimal.ZERO;

            // Get exchange rate
            exchangeRate = getExchangeRate(context);
            if (exchangeRate == null) {
                result = context.getResult();
                return !result.isOk();
            }

            if (khqrTransInfo.getCreditCurrency().equals(accountInfo.getAcctnCurrency())) {
                khqrTransInfo.setDebitAmount(khqrTransInfo.getAmount());
                khqrTransInfo.setDebitCurrency(accountInfo.getAcctnCurrency());
                khqrTransInfo.setCreditAmount(khqrTransInfo.getAmount());
                inputCcy = CommonServiceConstant.AmountType.DEBIT.name();
            }
            else {
                if (khqrTransInfo.getCurrency().equals(khqrTransInfo.getCreditCurrency())) {
                    khqrTransInfo.setCreditAmount(khqrTransInfo.getAmount());
                    inputCcy = CommonServiceConstant.AmountType.CREDIT.name();

                    khqrTransInfo.setDebitAmount(exchangeRate(khqrTransInfo.getAmount(),
                            khqrTransInfo.getCurrency(), exchangeRate, khqrTransInfo, RoundingMode.UP, inputCcy).toString());
                    khqrTransInfo.setDebitCurrency(accountInfo.getAcctnCurrency());

                }
                else if (khqrTransInfo.getCurrency().equals(accountInfo.getAcctnCurrency())) {
                    khqrTransInfo.setDebitAmount(khqrTransInfo.getAmount());
                    khqrTransInfo.setDebitCurrency(accountInfo.getAcctnCurrency());
                    inputCcy = CommonServiceConstant.AmountType.DEBIT.name();

                    khqrTransInfo.setCreditAmount(exchangeRate(khqrTransInfo.getAmount(),
                            khqrTransInfo.getCurrency(), exchangeRate, khqrTransInfo, RoundingMode.DOWN, inputCcy).toString());

                }
            }



            // Get Limit
            LimitTransInput limitTransInput = new LimitTransInput();
            limitTransInput.setCurrency(khqrTransInfo.getCreditCurrency());
            limitTransInput.setService(CommonServiceConstant.Service.CASA_TO_WALLET);
            limitTransInput.setCifT24(customer.getHostCifId());
            limitTransInput.setCustKycStatus(customer.getKycStatus());
            limitTransInput.setProvider(provider);
            limitTransInput.setChannel(CommonServiceConstant.CiftpChannel.RETAIL.name());
            limitTransInput.setQr(is_qr);

            ExecuteT24Output<LimitTransOutput> esbLimitOutput =
                    callFundsTransferService.getLimitTrans(provider, limitTransInput, customer.getId(), request.getRequestId());
            if (esbLimitOutput != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(esbLimitOutput.getStatus())) {
                limitTransOutput = esbLimitOutput.getData();
            }
            else {
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if (new BigDecimal(limitTransOutput.getMinTrxAmount())
                    .compareTo(new BigDecimal(khqrTransInfo.getCreditAmount())) == 1) {
                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_LMT_TRANS_MIN.getDesc(), false,
                        MBCResponseCode.CIFTP_VALID_LMT_TRANS_MIN.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if(Objects.nonNull(limitTransOutput.getMaxDayAmount())
                    && Double.valueOf(limitTransOutput.getMaxDayAmount()) > Double.valueOf(limitTransOutput.getMaxTrxAmount()))
            {
                if (new BigDecimal(khqrTransInfo.getCreditAmount())
                        .compareTo(new BigDecimal(limitTransOutput.getMaxTrxAmount())) >= 0) {
                    result = new SimpleResult(limitTransOutput.getMaxTrxAmount(), false,
                            MBCResponseCode.TRANSACTION_VALID_LMT_MAX.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                if(new BigDecimal(limitTransOutput.getMaxDayAmount())
                        .compareTo(new BigDecimal(khqrTransInfo.getCreditAmount())) == -1) {

                    result = new SimpleResult(MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getDesc(), false,
                            MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

            }else {
                if(new BigDecimal(limitTransOutput.getMaxDayAmount())
                        .compareTo(new BigDecimal(khqrTransInfo.getCreditAmount())) == -1) {

                    result = new SimpleResult(MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getDesc(), false,
                            MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
            }



//            if(Objects.nonNull(limitTransOutput.getMaxDayAmount())
//                && Double.valueOf(limitTransOutput.getMaxDayAmount()) > 0) {
//
//                if(new BigDecimal(limitTransOutput.getMaxDayAmount())
//                    .compareTo(new BigDecimal(bakongTransInfo.getCreditAmount())) == -1) {
//
//                    result = new SimpleResult(limitTransOutput.getMaxTrxAmount(), false,
//                        MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getErrorCode());
//                    context.setResult(result);
//                    return !result.isOk();
//                }
//            }


            // Get fee
            CiftpChargeInput input = new CiftpChargeInput();
            input.setAmount(khqrTransInfo.getCreditAmount());
            input.setCurrency(khqrTransInfo.getCreditCurrency());
            input.setFlow("SENDER");
            input.setService(CommonServiceConstant.Service.CASA_TO_WALLET);
            input.setQrPayment(StringUtils.isNotEmpty(khqrTransInfo.getPayloadQr()) ? Constant.YES : Constant.NO);
            input.setChannel(CommonServiceConstant.CiftpChannel.RETAIL.name());

            ExecuteT24Output<CiftpChargeOutput> domesticCharge =
                    callFundsTransferService.ciftpGwChargeTransfer(provider, input, customer.getId(), request.getRequestId());

            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(domesticCharge.getStatus())) {
                ciftpChargeOutput = domesticCharge.getData();
                chargeAmount = BigDecimal.valueOf(Double.valueOf(ciftpChargeOutput.getChargeAmount()));
                chargeCode = ciftpChargeOutput.getChargeCode();

                khqrTransInfo.setChargeAmount(ciftpChargeOutput.getChargeAmount());
                khqrTransInfo.setChargeCurrency(khqrTransInfo.getCreditCurrency());

                if (chargeAmount.compareTo(BigDecimal.ZERO) == 1) {
                    if (accountInfo.getAcctnCurrency().toUpperCase().equals(khqrTransInfo.getCreditCurrency())) {
//                        bakongTransInfo.setDebitChargeAmount(chargeAmount.toString());
                        debitChargeAmount = chargeAmount;
                    }
                    else {
                        if (Constant.CURRENCY_TYPE_KHR.equals(khqrTransInfo.getCreditCurrency())) {
//                            bakongTransInfo
//                                .setDebitChargeAmount();
                            debitChargeAmount = chargeAmount.divide(khqrTransInfo.getExchangeRate(), 2, RoundingMode.UP);
                        }

                        if (Constant.CURRENCY_TYPE_USD.equals(khqrTransInfo.getCreditCurrency())) {
//                            bakongTransInfo.setDebitChargeAmount();
                            debitChargeAmount = chargeAmount
                                    .multiply(khqrTransInfo.getExchangeRate()).setScale(0, RoundingMode.UP);
                        }
                    }

                    if(checkFreeChange(accountInfo, request.getSrvcCd(), request.getDigitalChannel())){
                        discountChargeAmount = new BigDecimal(debitChargeAmount.toString());
                        debitChargeAmount = BigDecimal.ZERO;
                        campaignCode = "VIP_ACCOUNT_FREE_FEE";
                    }

                    khqrTransInfo.setDebitChargeAmount(debitChargeAmount.toString());

                }
                else {
                    debitChargeAmount = BigDecimal.ZERO;
                    khqrTransInfo.setDebitChargeAmount(BigDecimal.ZERO.toString());
                }
            }
            else if ("4936".equals(domesticCharge.getErrorInfo().getErrorCode())) {
                result = new SimpleResult(MBCResponseCode.CHARGE_MAX.getDesc(), false,
                        MBCResponseCode.CHARGE_MAX.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            else {
                result = new SimpleResult(domesticCharge.getErrorInfo().getErrorDesc(), false,
                        domesticCharge.getErrorInfo().getErrorCode());
                context.setResult(result);
                return !result.isOk();
            }

            totalDebitAmount = new BigDecimal(khqrTransInfo.getDebitAmount())
                    .add(new BigDecimal(khqrTransInfo.getDebitChargeAmount()));


            BigDecimal available = new BigDecimal(accountInfo.getBalance().getAvailable());
            if (totalDebitAmount.compareTo(available) == 1) {
                result = new SimpleResult(MBCResponseCode.INVALID_AVAILABLE_BALANCE.getDesc(), false,
                        MBCResponseCode.INVALID_AVAILABLE_BALANCE.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            // TAO BAN GHI COM_TRANS
            // transRequest.setChargeAmount(String.valueOf(chargeAmount));

            ComTrans transInfo = objectMapper.convertValue(khqrTransInfo, ComTrans.class);
            transInfo.setSessionId(request.getSessionId());
            transInfo.setCustId(customer.getId());
            transInfo.setCreatedBy(customer.getUserId());
            transInfo.setStatus(Constant.COM_STATUS_INT);
            transInfo.setSrvcCd(request.getSrvcCd());
            transInfo.setTransferType(CommonServiceConstant.TransferType.CIFTP.name());
            transInfo.setTransactionType(CommonServiceConstant.TransactionType.ACCI.name());
            transInfo.setChannel(request.getDigitalChannel());
            transInfo.setPartnerCode(request.getPartnerSdk());
            comTransRepo.save(transInfo);

            // TAO BAN GHI COM_TRANS_DTL_TRANSFER
            ComTransDtlTransfer transDtlInfo = objectMapper.convertValue(khqrTransInfo, ComTransDtlTransfer.class);
            transDtlInfo.setId(transInfo.getId());
            transDtlInfo.setCustId(customer.getId());
            transDtlInfo.setCreatedBy(customer.getUserId());
            transDtlInfo.setStatus(Constant.COM_STATUS_INT);
            transDtlInfo.setOwnerCharge(khqrTransInfo.isOwnerCharge() ? "1" : "0");
            transDtlInfo.setChargeAmount(chargeAmount);
            transDtlInfo.setChargeCode(chargeCode);
            transDtlInfo.setAmountType(inputCcy);
            transDtlInfo.setTransferType(CommonServiceConstant.TransferType.CIFTP.name());
            transDtlInfo.setTransactionType(CommonServiceConstant.TransactionType.ACCI.name());
            transDtlInfo.setCiftpChannel(CommonServiceConstant.CiftpChannel.RETAIL.name());
            transDtlInfo.setTransferService(CommonServiceConstant.Service.CASA_TO_WALLET.name());
            transDtlInfo.setDebitAcctType(CommonServiceConstant.AccountType.ACCOUNT.name());
            transDtlInfo.setCreditAcctType(CommonServiceConstant.AccountType.WALLET.name());
            transDtlInfo.setQrPayType(khqrTransInfo.getQrPayType());
            transDtlInfo.setRate(khqrTransInfo.getRate());
            transDtlInfo.setExchangeRate(khqrTransInfo.getExchangeRate());
            transDtlInfo.setDebitChargeAmount(debitChargeAmount.toString());
            transDtlInfo.setDiscountChargeAmount(discountChargeAmount);
            transDtlInfo.setCampaignCode(campaignCode);

            comTransDtlTransferRepo.saveAndFlush(transDtlInfo);

            // TAO BAN GHI COM_TRANS_PROCESS
            ComTransProcess comTransProcess = new ComTransProcess();
            comTransProcess.setStatus(Constant.COM_STATUS_INT);
            comTransProcess.setTransId(transInfo.getId());
            comTransProcess.setSrvcCd(transInfo.getSrvcCd());
            comTransProcessRepo.saveAndFlush(comTransProcess);

            ComTransDomesticCharge comTransDomesticCharge = new ComTransDomesticCharge();
            comTransDomesticCharge.setTransId(transInfo.getId());
            comTransDomesticCharge.setChargeCode(ciftpChargeOutput.getChargeCode());
            comTransDomesticCharge.setChargeCurrency(ciftpChargeOutput.getChargeCurrency());
            comTransDomesticCharge
                    .setChargeAmount(BigDecimal.valueOf(Double.valueOf(ciftpChargeOutput.getChargeAmount())));
            comTransDomesticChargeRepo.saveAndFlush(comTransDomesticCharge);

            response.setTransId(transInfo.getId());
            response.setTransTime(String.valueOf(transInfo.getCreatedDt().getTime()));
            RedisServer.saveCacheRedis(transInfo.getId(), String.valueOf(transInfo.getCreatedDt().getTime()), 5);
        }
        catch (Exception e) {
            log.error("[Exception validate to wallet] requestId: {} desc: {}", request.getRequestId(), e.getStackTrace());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
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

    private RateT24 getExchangeRate(ProcessContext context) throws Exception {
        RateT24 exchangeRate = null;
        Result result = null;
        String dataRate = RedisServer.getCacheRedis(RedisServer.CURRENCY_RATE);
        List<RateT24> lstExchangeRate = new ArrayList<>();
        if (!Utility.isNull(dataRate)) {
            lstExchangeRate = Arrays.asList(objectMapper.readValue(dataRate, RateT24[].class));
        }
        else {
            RateCurrencyInput message = new RateCurrencyInput();
            message.setDate(DateFormatUtils.format(new Date(), DateUtil.DATE_8_CHAR_REVERSE));
            ExecuteT24Output<RateCurrencyT24> output = null;
            output = callILT24Service.getRateCurrency(message, context.getCustomer().getId(),
                    context.getRequest().getRequestId());
            if (output != null) {
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                    RateCurrencyT24 rateT24 = output.getData();
                    lstExchangeRate = rateT24.getRateDTOList();
                    String data = objectMapper.writeValueAsString(rateT24.getRateDTOList());
                    RedisServer.saveCacheRedis(RedisServer.CURRENCY_RATE, data, 5);
                }
                else {
                    result = new SimpleResult(
                            output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail(), false,
                            output.getErrorInfo().getErrorCode());
                    context.setResult(result);
                }
            }
        }

        for (RateT24 rateT24 : lstExchangeRate) {
            if (Constant.CURRENCY_TYPE_KHR.equals(rateT24.getRefCurr())
                    && Constant.CURRENCY_TYPE_USD.equals(rateT24.getSourceCurr())) {
                exchangeRate = rateT24;
                break;
            }
        }

        return exchangeRate;
    }

    public BigDecimal exchangeRate(String amount, String currency, RateT24 rate, TransInfo bakongTransInfo,
                                   RoundingMode roundingMode, String inputCcy) {

        BigDecimal exchange_amount = BigDecimal.ZERO;

        if (CommonServiceConstant.AmountType.CREDIT.name().equals(inputCcy)) {

            if (Constant.CURRENCY_TYPE_KHR.equals(currency)) {

                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(Double.valueOf(rate.getSellRate())), 0, RoundingMode.HALF_UP);

                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount))
                        .divide(exchangeRate, 2, roundingMode);
                bakongTransInfo.setRate(rate.getSellRate());
                bakongTransInfo.setExchangeRate(exchangeRate);

            }

            if (Constant.CURRENCY_TYPE_USD.equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(Double.valueOf(rate.getBuyRate())), 0, RoundingMode.HALF_UP);

                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount)).multiply(exchangeRate).setScale(0, roundingMode);
                bakongTransInfo.setRate(rate.getBuyRate());
                bakongTransInfo.setExchangeRate(exchangeRate);

            }

        }

        if (CommonServiceConstant.AmountType.DEBIT.name().equals(inputCcy)) {

            if (Constant.CURRENCY_TYPE_KHR.equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(Double.valueOf(rate.getBuyRate())), 0, RoundingMode.HALF_UP);

                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount))
                        .divide(exchangeRate, 2, roundingMode);
                bakongTransInfo.setRate(rate.getBuyRate());
                bakongTransInfo.setExchangeRate(exchangeRate);

            }

            if (Constant.CURRENCY_TYPE_USD.equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(Double.valueOf(rate.getSellRate())), 0, RoundingMode.HALF_UP);

                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount)).multiply(exchangeRate).setScale(0, roundingMode);
                bakongTransInfo.setRate(rate.getSellRate());
                bakongTransInfo.setExchangeRate(exchangeRate);

            }
        }

        // if (Constant.CURRENCY_TYPE_KHR.equals(currency)) {
        // exchange_amount = BigDecimal.valueOf(Double.valueOf(amount))
        // .multiply(BigDecimal.valueOf(Double.valueOf(rate.getSellRate()))).setScale(2, roundingMode);
        // bakongTransInfo.setRate(rate.getSellRate());
        //
        // }
        //
        //
        // if (Constant.CURRENCY_TYPE_USD.equals(currency)) {
        // exchange_amount = BigDecimal.valueOf(Double.valueOf(amount)).divide(new BigDecimal(rate.getBuyRate()), 0,
        // roundingMode);
        // bakongTransInfo.setRate(rate.getBuyRate());
        //
        // }

        // if (Constant.CURRENCY_TYPE_KHR.equals(currency)) {
        // exchange_amount = BigDecimal.valueOf(Double.valueOf(amount))
        // .multiply(BigDecimal.valueOf(Double.valueOf(rate.getSellRate()))).setScale(2, roundingMode);
        // bakongTransInfo.setRate(rate.getSellRate());
        //
        // }
        //
        // if (Constant.CURRENCY_TYPE_USD.equals(currency)) {
        // exchange_amount = BigDecimal.valueOf(Double.valueOf(amount)).divide(new BigDecimal(rate.getBuyRate()), 0,
        // roundingMode);
        // bakongTransInfo.setRate(rate.getBuyRate());
        //
        // }

        return exchange_amount;
    }

    public boolean checkFreeChange(AccountBase account, String service, String channel){
        ComFreeTrxVipAcct comFreeTrxVipAcct = comFreeTrxVipAcctRepo.findByAcctNumberAndStatusAndChannel(account.getAcctId(), Constant.STATUS_1, channel);
        ComServiceFreeVipAcct comServiceFreeVipAcct = comServiceFreeVipAcctRepo.findByServiceAndStatus(service, Constant.STATUS_1);

        if(Objects.nonNull(comFreeTrxVipAcct) && Objects.nonNull(comServiceFreeVipAcct)){
            Date openDateAcct = DateUtil.convertStringToDate(account.getOpenDate(), DateUtil.DATE_SIMPLE_REVERSE);
            if (DateCompareUtil.daysDiff(openDateAcct, new Date()) < 365){
                return true;
            }
        }
        return false;
    }

}
