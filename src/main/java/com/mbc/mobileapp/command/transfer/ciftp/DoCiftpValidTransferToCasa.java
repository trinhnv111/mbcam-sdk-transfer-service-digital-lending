
package com.mbc.mobileapp.command.transfer.ciftp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.api.CallILT24Service;
import com.mbc.common.api.models.exchangerate.RateCurrencyInput;
import com.mbc.common.api.models.exchangerate.RateCurrencyT24;
import com.mbc.common.api.models.exchangerate.RateT24;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.*;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.*;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator.Result;
import com.mbc.gateway.validator.result.SimpleResult;

import com.mbc.mobileapp.api.CallFundsTransferService;
import com.mbc.mobileapp.api.model.transfer.ciftp.*;

import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import com.mbc.mobileapp.rest.transfer.ciftp.CiftpChannelLimit;
import kh.org.nbc.bakong_khqr.BakongKHQR;
import kh.org.nbc.bakong_khqr.model.CRCValidation;
import kh.org.nbc.bakong_khqr.model.KHQRResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@Transactional
public class DoCiftpValidTransferToCasa implements Command {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CallILT24Service callILT24Service;

    @Autowired
    private CallFundsTransferService callFundsTransferService;

    @Value("${provider.domestic.transfer.ciftp}")
    private String provider;

    @Autowired
    private ComTransRepo comTransRepo;

    @Autowired
    private ComTransProcessRepo comTransProcessRepo;

    @Autowired
    private ComTransDtlTransferRepo comTransDtlTransferRepo;

    @Autowired
    private ComTransDomesticChargeRepo comTransDomesticChargeRepo;

    @Autowired
    private ComFreeTrxVipAcctRepo comFreeTrxVipAcctRepo;

    @Autowired
    private ComServiceFreeVipAcctRepo comServiceFreeVipAcctRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Result result = Result.OK;

        CustInfo customer = context.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        TransInfo ciftpTransInfo = request.getTransInfo();
        LimitTransInput limitTransInput = new LimitTransInput();
        RateT24 exchangeRate = null;
        String ciftpChannel = null;
        List<String> ciftpChannelAvailable = new ArrayList<String>();
        CiftpBankInfo ciftpBankInfo = null;
        CiftpChannelLimit ciftpChannelLimit = new CiftpChannelLimit();
        BigDecimal debitChargeAmount = null;
        BigDecimal discountChargeAmount = null;
        String campaignCode = null;
        boolean checkChannelLimit = false;

        try {

            if (!Utility.isValidAmount(ciftpTransInfo.getAmount())) {
                result = new SimpleResult(MBCResponseCode.AMOUNT_INVALID.getDesc(), false,
                    MBCResponseCode.AMOUNT_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

//            if (!Utility.isNull(ciftpTransInfo.getPayloadQr())) {
//                KHQRResponse<CRCValidation> khqrResponse = BakongKHQR.verify(ciftpTransInfo.getPayloadQr());
//                if (!khqrResponse.getData().isValid()) {
//                    result = new SimpleResult(MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getDesc(), false,
//                            MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getCode());
//                    context.setResult(result);
//                    return !result.isOk();
//                }
//            }


            if(!response.getCiftpAccountInfo().getAccountName().equals(ciftpTransInfo.getCreditAcctName())) {
                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_BENE.getDesc(), false,
                        MBCResponseCode.CIFTP_VALID_INFO_BENE.getCode());
                context.setResult(result);
                return !result.isOk();
            }

//            if(!response.getCiftpAccountInfo().getBankName().equals(ciftpTransInfo.getDestBankName())) {
//                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_BENE.getDesc(), false,
//                    MBCResponseCode.CIFTP_VALID_INFO_BENE.getCode());
//                context.setResult(result);
//                return !result.isOk();
//            }

            if(!response.getCiftpAccountInfo().getBankParticipantCode().equals(ciftpTransInfo.getDestBankPartiCode())) {
                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_BENE.getDesc(), false,
                        MBCResponseCode.CIFTP_VALID_INFO_BENE.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if(!response.getCiftpAccountInfo().getBankCode().equals(ciftpTransInfo.getDestBankCode())) {
                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_BENE.getDesc(), false,
                        MBCResponseCode.CIFTP_VALID_INFO_BENE.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            AccountBase accountInfo = (AccountBase) context.getVar(Constant.KeyVar.DEBIT_ACCOUNT_INFO);
            response.setCurrency(accountInfo.getAcctnCurrency());

            BigDecimal chargeAmount = BigDecimal.ZERO;
            String chargeCode = null;
            String inputCcy = null;

            // Get exchange rate
            exchangeRate = getExchangeRate(context);
            if (exchangeRate == null) {
                result = context.getResult();
                return !result.isOk();
            }

            if (ciftpTransInfo.getCreditCurrency().equals(accountInfo.getAcctnCurrency())) {

                //Same CCY: check ccy amount # ccy tk nguồn và tk nhận
                if(!accountInfo.getAcctnCurrency().equals(ciftpTransInfo.getCurrency())){
                    log.error("[ERROR] Curency invalid requestId: {}", request.getRequestId());
                    result = new SimpleResult(MBCResponseCode.CURRENCY_INVALID.getDesc(), false,
                            MBCResponseCode.CURRENCY_INVALID.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                ciftpTransInfo.setDebitAmount(ciftpTransInfo.getAmount());
                ciftpTransInfo.setDebitCurrency(accountInfo.getAcctnCurrency());
                ciftpTransInfo.setCreditAmount(ciftpTransInfo.getAmount());
                inputCcy = CommonServiceConstant.AmountType.DEBIT.name();
            }
            else {
                if (ciftpTransInfo.getCurrency().equals(ciftpTransInfo.getCreditCurrency())) {
                    ciftpTransInfo.setCreditAmount(ciftpTransInfo.getAmount());
                    inputCcy = CommonServiceConstant.AmountType.CREDIT.name();

                    ciftpTransInfo.setDebitAmount(exchangeRate(ciftpTransInfo.getAmount(),
                            ciftpTransInfo.getCurrency(), exchangeRate, ciftpTransInfo, RoundingMode.UP, inputCcy).toString());
                    ciftpTransInfo.setDebitCurrency(accountInfo.getAcctnCurrency());

                }
                else if (ciftpTransInfo.getCurrency().equals(accountInfo.getAcctnCurrency())) {
                    ciftpTransInfo.setDebitAmount(ciftpTransInfo.getAmount());
                    ciftpTransInfo.setDebitCurrency(accountInfo.getAcctnCurrency());
                    inputCcy = CommonServiceConstant.AmountType.DEBIT.name();

                    ciftpTransInfo.setCreditAmount(exchangeRate(ciftpTransInfo.getAmount(),
                            ciftpTransInfo.getCurrency(), exchangeRate, ciftpTransInfo, RoundingMode.DOWN, inputCcy).toString());

                }
            }

            //GET BANK INFO
            String jsonBank = RedisServer.getCacheRedis(RedisServer.CIFTP_GET_BANK_LIST);
            List<CiftpBankInfo> ciftpListBank = new ArrayList<CiftpBankInfo>();
            if(Objects.nonNull(jsonBank)) {
                ciftpListBank = Arrays.asList(JSON.parseObject(jsonBank, CiftpBankInfo[].class));
                for (CiftpBankInfo bankInfo : ciftpListBank) {
                    if(bankInfo.getParticipantCode().equals(ciftpTransInfo.getDestBankPartiCode())) {
                        ciftpBankInfo = bankInfo;
                        break;
                    }
                }
            }else {
                result = new SimpleResult(ResponseCode.TRANSFER_EXPIRED_TIME.getDesc(), false,
                        ResponseCode.TRANSFER_EXPIRED_TIME.getCode());
                context.setResult(result);
                return !result.isOk();
            }


            //CHECK CIFTP CHANNEL
            for (CiftpMakeConfigInfo ciftpMakeConfigInfo : response.getLstMakeTransferConfig()) {
                if(ciftpMakeConfigInfo.getCurrency().equals(ciftpTransInfo.getCreditCurrency())) {
                    if(Double.valueOf(ciftpMakeConfigInfo.getAmountMin()) <= Double.valueOf(ciftpTransInfo.getCreditAmount())
                            && Double.valueOf(ciftpMakeConfigInfo.getAmountMax()) > Double.valueOf(ciftpTransInfo.getCreditAmount()))
                    {
                        ciftpChannelAvailable.add(ciftpMakeConfigInfo.getChannel().toUpperCase());
                        ciftpChannel = ciftpMakeConfigInfo.getChannel().toUpperCase();
                    }

                }
            }


            if(ciftpChannelAvailable.size() == 0) {
                result = new SimpleResult(MBCResponseCode.AMOUNT_INVALID.getDesc(), false,
                        MBCResponseCode.AMOUNT_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            //CHECK RETAIL CHANNEL
            if(ciftpChannelAvailable.indexOf(CommonServiceConstant.CiftpChannel.RETAIL.name()) != -1){
                ciftpChannel = CommonServiceConstant.CiftpChannel.RETAIL.name();
                ciftpChannelAvailable.clear();
                ciftpChannelAvailable.add(CommonServiceConstant.CiftpChannel.RETAIL.name());
            }

            if(Objects.nonNull(ciftpTransInfo.getCiftpChannel())) {
                if(ciftpChannelAvailable.contains(ciftpTransInfo.getCiftpChannel())) {
                    ciftpChannel = ciftpTransInfo.getCiftpChannel();
                    checkChannelLimit = true;
                }else {
                    result = new SimpleResult(MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT.getDesc(), false,
                            MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
            }else {
                //CHECK AVAILABLE CIFTP CHANNEL
                if (!CommonServiceConstant.CiftpChannel.RETAIL.name().equals(ciftpChannel)) {
                    if (ciftpChannelAvailable.size() == 2) {
                        boolean LARGE_VALUE =
                                ciftpBankInfo.getPublicOperations().contains(CommonServiceConstant.CiftpChannel.LARGE_VALUE.name());
                        boolean NCS = Objects.isNull(ciftpBankInfo.getNcsBankCode()) ? false : true;

                        if (!LARGE_VALUE) {
                            ciftpChannelAvailable.remove(CommonServiceConstant.CiftpChannel.LARGE_VALUE.name());
                        }

                        if (!NCS) {
                            ciftpChannelAvailable.remove(CommonServiceConstant.CiftpChannel.NCS.name());
                        }

                        if(ciftpChannelAvailable.size() == 2) {
                            ciftpChannel = CommonServiceConstant.CiftpChannel.LARGE_VALUE.name();
                        }

                        if(ciftpChannelAvailable.size() == 1) {
                            ciftpChannel = ciftpChannelAvailable.get(0);
                        }

                        if(ciftpChannelAvailable.size() == 0) {
                            result = new SimpleResult(MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT_LV_NCS.getDesc(), false,
                                    MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT_LV_NCS.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }


                    }

                    if (ciftpChannelAvailable.size() == 1) {
                        if(CommonServiceConstant.CiftpChannel.LARGE_VALUE.name().equals(ciftpChannelAvailable.get(0))) {
                            boolean LARGE_VALUE = ciftpBankInfo.getPublicOperations().contains(CommonServiceConstant.CiftpChannel.LARGE_VALUE.name());
                            if (!LARGE_VALUE) {

                                result = new SimpleResult(MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT.getDesc(), false,
                                        MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT.getCode());
                                context.setResult(result);
                                return !result.isOk();
                            }

                            ciftpChannel = CommonServiceConstant.CiftpChannel.LARGE_VALUE.name();
                        }else {
                            boolean NCS = Objects.isNull(ciftpBankInfo.getNcsBankCode()) ? false : true;
                            if (!NCS) {
                                result = new SimpleResult(MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT.getDesc(), false,
                                        MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT.getCode());
                                context.setResult(result);
                                return !result.isOk();
                            }

                            ciftpChannel = CommonServiceConstant.CiftpChannel.NCS.name();
                        }
                    }
                }else {
                    boolean RETAIL = false;
                    if(ciftpBankInfo.getPublicOperations().contains("MOBILE_DEPOSIT")
                            && ciftpBankInfo.getPublicOperations().contains("DESKTOP_DEPOSIT")) {
                        RETAIL = true;
                    }

                    if (!RETAIL) {
                        result = new SimpleResult(MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT.getDesc(), false,
                                MBCResponseCode.CIFTP_CHANNEL_BANK_NOT_SUPPORT.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }

                    ciftpChannel = CommonServiceConstant.CiftpChannel.RETAIL.name();
                }

                ciftpTransInfo.setCiftpChannel(ciftpChannel);
            }
            
            //CHAN KHONG CHO CHUYEN QUA NCS
//            if(CiftpChannel.NCS.name().equals(ciftpChannel)) {
//                result = new SimpleResult(MBCResponseCode.CIFTP_OVER_LMT_PER_TRANS.getDesc(), false,
//                    MBCResponseCode.CIFTP_OVER_LMT_PER_TRANS.getCode());
//                context.setResult(result);
//                return !result.isOk();
//            }

//            if(ciftpChannelAvailable.contains(ServiceConstant.CiftpChannel.NCS.name())) {
//                ciftpChannelAvailable.remove(ServiceConstant.CiftpChannel.NCS.name());
//            }
            
           
            if(ciftpChannelAvailable.size() > 1 && !checkChannelLimit) {
                for (String channel : ciftpChannelAvailable) {
                    // input limit
                    limitTransInput.setService(CommonServiceConstant.Service.CASA_TO_CASA);
                    limitTransInput.setCustKycStatus(customer.getKycStatus());
                    limitTransInput.setCifT24(customer.getHostCifId());
                    limitTransInput.setCurrency(ciftpTransInfo.getCreditCurrency());
                    limitTransInput.setProvider(provider);
                    limitTransInput.setChannel(channel);
                    limitTransInput.setQr("NO");

                    result = processLimit(provider, limitTransInput, customer, request.getRequestId(), accountInfo,
                            ciftpTransInfo, ciftpChannelLimit);

                    if(result.isOk()) {
                        if(CommonServiceConstant.CiftpChannel.LARGE_VALUE.name().equals(channel)) {
                            ciftpChannelLimit.setLargeValue("OK");
                        }

                        if(CommonServiceConstant.CiftpChannel.NCS.name().equals(channel)) {
                            ciftpChannelLimit.setNcs("OK");
                        }
                    }else if (MBCResponseCode.TRANSACTION_VALID_LMT_MIN.getCode().equals(result.getResponseCode())) {
                        if(CommonServiceConstant.CiftpChannel.LARGE_VALUE.name().equals(channel)) {
                            ciftpChannelLimit.setLargeValue("MIN_TRX");
                        }

                        if(CommonServiceConstant.CiftpChannel.NCS.name().equals(channel)) {
                            ciftpChannelLimit.setNcs("MIN_TRX");
                        }
                    }else if (MBCResponseCode.TRANSACTION_VALID_LMT_MAX.getCode().equals(result.getResponseCode())) {
                        if(CommonServiceConstant.CiftpChannel.LARGE_VALUE.name().equals(channel)) {
                            ciftpChannelLimit.setLargeValue("MAX_TRX");
                        }

                        if(CommonServiceConstant.CiftpChannel.NCS.name().equals(channel)) {
                            ciftpChannelLimit.setNcs("MAX_TRX");
                        }
                    }else if (MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getCode().equals(result.getResponseCode())) {
                        if(CommonServiceConstant.CiftpChannel.LARGE_VALUE.name().equals(channel)) {
                            ciftpChannelLimit.setLargeValue("MAX_DAY");
                        }

                        if(CommonServiceConstant.CiftpChannel.NCS.name().equals(channel)) {
                            ciftpChannelLimit.setNcs("MAX_DAY");
                        }
                    }
                }
                ciftpTransInfo.setCiftpChannelLimit(ciftpChannelLimit);
                if("OK".equals(ciftpChannelLimit.getLargeValue())) {
                    ciftpChannel = CommonServiceConstant.CiftpChannel.LARGE_VALUE.name();
                    ciftpTransInfo.setCiftpChannel(ciftpChannel);

                    result = Result.OK;

                }else if("OK".equals(ciftpChannelLimit.getNcs())) {
                    ciftpChannel = CommonServiceConstant.CiftpChannel.NCS.name();
                    ciftpTransInfo.setCiftpChannel(ciftpChannel);
                    result = Result.OK;

                }else {
                    ciftpChannel = null;
                    result = new SimpleResult(MBCResponseCode.CIFTP_CHECK_LIMIT_CHANNEL_INVALID.getDesc(), false,
                            MBCResponseCode.CIFTP_CHECK_LIMIT_CHANNEL_INVALID.getCode());
                    request.setTransInfo(ciftpTransInfo);
                    context.setResult(result);
                    return !result.isOk();
                }


            }else {
                limitTransInput.setService(CommonServiceConstant.Service.CASA_TO_CASA);
                limitTransInput.setCustKycStatus(customer.getKycStatus());
                limitTransInput.setCifT24(customer.getHostCifId());
                limitTransInput.setCurrency(ciftpTransInfo.getCreditCurrency());
                limitTransInput.setProvider(provider);
                limitTransInput.setChannel(ciftpChannel);
                limitTransInput.setQr("NO");

                result = processLimit(provider, limitTransInput, customer, request.getRequestId(), accountInfo,
                        ciftpTransInfo, ciftpChannelLimit);

                if(!result.isOk()) {
                    context.setResult(result);
                    return !result.isOk();
                }
            }

            ciftpTransInfo.setCiftpChannelAvailable(ciftpChannelAvailable);
            if(CommonServiceConstant.CiftpChannel.NCS.name().equals(ciftpChannel)) {
                ciftpTransInfo.setPaymentTypeCode(
                        Constant.CURRENCY_TYPE_USD.equals(ciftpTransInfo.getCreditCurrency()) ? "602000" : "601000");
            }

            CiftpChargeOutput ciftpChargeOutput = null;
            // GET FEE TRANSACTION
            CiftpChargeInput input = new CiftpChargeInput();
            input.setAmount(ciftpTransInfo.getCreditAmount());
            input.setCurrency(ciftpTransInfo.getCreditCurrency());
            input.setFlow("SENDER");
            input.setService(CommonServiceConstant.Service.CASA_TO_CASA);
            input.setQrPayment(StringUtils.isNotEmpty(ciftpTransInfo.getPayloadQr()) ? Constant.YES : Constant.NO);
            input.setChannel(ciftpChannel);
            input.setPaymentTypeCode(ciftpTransInfo.getPaymentTypeCode());

            ExecuteT24Output<CiftpChargeOutput> domesticCharge =
                    callFundsTransferService.ciftpGwChargeTransfer(provider, input, customer.getId(), request.getRequestId());

            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(domesticCharge.getStatus())) {
                ciftpChargeOutput = domesticCharge.getData();
                chargeAmount = new BigDecimal(ciftpChargeOutput.getChargeAmount());
                chargeCode = ciftpChargeOutput.getChargeCode();
                ciftpTransInfo.setChargeAmount(chargeAmount.toString());
                ciftpTransInfo.setChargeCurrency(ciftpTransInfo.getCreditCurrency());

                if (accountInfo.getAcctnCurrency().toUpperCase().equals(ciftpTransInfo.getCreditCurrency())) {
//                    ciftpTransInfo.setDebitChargeAmount(chargeAmount.toString());
                    debitChargeAmount = chargeAmount;
                }

                else {
                    if (Constant.CURRENCY_TYPE_KHR.equals(ciftpTransInfo.getCreditCurrency())) {
                        debitChargeAmount = chargeAmount.divide(ciftpTransInfo.getExchangeRate(), 2, RoundingMode.UP);

                    }

                    if (Constant.CURRENCY_TYPE_USD.equals(ciftpTransInfo.getCreditCurrency())) {
                        debitChargeAmount = chargeAmount
                                .multiply(ciftpTransInfo.getExchangeRate()).setScale( 0, RoundingMode.UP);
                    }
                }

                if(checkFreeChange(accountInfo, request.getSrvcCd(), request.getDigitalChannel())){
                    discountChargeAmount = new BigDecimal(debitChargeAmount.toString());
                    debitChargeAmount = BigDecimal.ZERO;
                    campaignCode = "VIP_ACCOUNT_FREE_FEE";
                }

                ciftpTransInfo
                        .setDebitChargeAmount(debitChargeAmount.toString());


                BigDecimal totalDebitAmount = new BigDecimal(ciftpTransInfo.getDebitAmount())
                        .add(debitChargeAmount);
                BigDecimal available = new BigDecimal(accountInfo.getBalance().getAvailable());
                if (totalDebitAmount.compareTo(available) == 1) {
                    result = new SimpleResult(MBCResponseCode.INVALID_AVAILABLE_BALANCE.getDesc(), false,
                            MBCResponseCode.INVALID_AVAILABLE_BALANCE.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }


                if (result.isOk()) {

                    // save thong tin chung giao dich co tinh phi
                    ComTrans transInfo = objectMapper.convertValue(ciftpTransInfo, ComTrans.class);
                    transInfo.setSessionId(request.getSessionId());
                    transInfo.setCustId(customer.getId());
                    transInfo.setCreatedBy(customer.getUserId());
                    transInfo.setStatus(Constant.COM_STATUS_INT);
                    transInfo.setSrvcCd(request.getSrvcCd());
                    transInfo.setTransferType(CommonServiceConstant.TransferType.CIFTP.name());
                    transInfo.setTransactionType(CommonServiceConstant.TransactionType.ACCI.name());
                    transInfo.setChannel(request.getDigitalChannel());
                    transInfo.setPartnerCode(request.getPartnerSdk());
                    transInfo = comTransRepo.save(transInfo);

                    // TAO BAN GHI COM_TRANS_DTL_TRANSFER
                    ComTransDtlTransfer transDtlInfo =
                            objectMapper.convertValue(ciftpTransInfo, ComTransDtlTransfer.class);
                    transDtlInfo.setId(transInfo.getId());
                    transDtlInfo.setCustId(customer.getId());
                    transDtlInfo.setCreatedBy(customer.getUserId());
                    transDtlInfo.setStatus(Constant.COM_STATUS_INT);
                    transDtlInfo.setOwnerCharge(ciftpTransInfo.isOwnerCharge() ? "1" : "0");
                    transDtlInfo.setChargeAmount(chargeAmount);
                    transDtlInfo.setChargeCode(chargeCode);
                    transDtlInfo.setDebitChargeAmount(debitChargeAmount.toString());
                    transDtlInfo.setDiscountChargeAmount(discountChargeAmount);
                    transDtlInfo.setRate(ciftpTransInfo.getRate());
                    transDtlInfo.setAmountType(inputCcy);
                    transDtlInfo.setCreditAmount(new BigDecimal(ciftpTransInfo.getCreditAmount()));
                    transDtlInfo.setDebitAmount(new BigDecimal(ciftpTransInfo.getDebitAmount()));
                    transDtlInfo.setTransferType(CommonServiceConstant.TransferType.CIFTP.name());
                    transDtlInfo.setTransactionType(CommonServiceConstant.TransactionType.ACCI.name());
                    transDtlInfo.setTransferService(CommonServiceConstant.Service.CASA_TO_CASA.name());
                    transDtlInfo.setCiftpChannel(ciftpTransInfo.getCiftpChannel());
                    transDtlInfo.setPaymentTypeCode(ciftpTransInfo.getPaymentTypeCode());
                    transDtlInfo.setDebitAcctType(CommonServiceConstant.AccountType.ACCOUNT.name());
                    transDtlInfo.setCreditAcctType(CommonServiceConstant.AccountType.ACCOUNT.name());
                    transDtlInfo.setPayloadQr(
                            !StringUtils.isEmpty(ciftpTransInfo.getPayloadQr()) ? ciftpTransInfo.getPayloadQr() : "");
                    transDtlInfo.setCampaignCode(campaignCode);
                    comTransDtlTransferRepo.saveAndFlush(transDtlInfo);

                    // TAO BAN GHI COM_TRANS_PROCESS
                    ComTransProcess comTransProcess = new ComTransProcess();
                    comTransProcess.setStatus(Constant.COM_STATUS_INT);
                    comTransProcess.setTransId(transInfo.getId());
                    comTransProcess.setSrvcCd(transInfo.getSrvcCd());
                    comTransProcessRepo.saveAndFlush(comTransProcess);
                    // save fee
                    ComTransDomesticCharge comTransDomesticCharge = new ComTransDomesticCharge();
                    comTransDomesticCharge.setTransId(transInfo.getId());
                    comTransDomesticCharge.setChargeCode(chargeCode);
                    comTransDomesticCharge.setChargeCurrency(ciftpChargeOutput.getChargeCurrency());
                    comTransDomesticCharge.setChargeAmount(chargeAmount);
                    comTransDomesticChargeRepo.saveAndFlush(comTransDomesticCharge);

                    response.setTransId(transInfo.getId());
                    response.setTransTime(String.valueOf(transInfo.getCreatedDt().getTime()));
                    RedisServer.saveCacheRedis(transInfo.getId(), String.valueOf(transInfo.getCreatedDt().getTime()), 5);

                    request.setTransInfo(ciftpTransInfo);
                    context.setRequest(request);
                    context.setResponse(response);
                }
                context.setResult(result);
                return !result.isOk();
            }
            else if ("4936".equals(domesticCharge.getErrorInfo().getErrorDesc())) {
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
        }
        catch (Exception e) {
            AppLog.error("[Exception validate to casa] requestId: "+request.getRequestId()+" desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());

        }
        context.setRequest(request);
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

    public BigDecimal exchangeRate(String amount, String currency, RateT24 rate, TransInfo ciftpTransInfo,
                                   RoundingMode roundingMode, String inputCcy) {

        BigDecimal exchange_amount = BigDecimal.ZERO;

        if (CommonServiceConstant.AmountType.CREDIT.name().equals(inputCcy)) {

            if (Constant.CURRENCY_TYPE_KHR.equals(currency)) {

                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(Double.valueOf(rate.getSellRate())), 0, RoundingMode.HALF_UP);

                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount))
                    .divide(exchangeRate, 2, roundingMode);
                ciftpTransInfo.setRate(rate.getSellRate());
                ciftpTransInfo.setExchangeRate(exchangeRate);

            }

            if (Constant.CURRENCY_TYPE_USD.equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(Double.valueOf(rate.getBuyRate())), 0, RoundingMode.HALF_UP);

                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount)).multiply(exchangeRate).setScale(0, roundingMode);
                ciftpTransInfo.setRate(rate.getBuyRate());
                ciftpTransInfo.setExchangeRate(exchangeRate);

            }

        }

        if (CommonServiceConstant.AmountType.DEBIT.name().equals(inputCcy)) {

            if (Constant.CURRENCY_TYPE_KHR.equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                        BigDecimal.valueOf(Double.valueOf(rate.getBuyRate())), 0, RoundingMode.HALF_UP);

                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount))
                        .divide(exchangeRate, 2, roundingMode);
                ciftpTransInfo.setRate(rate.getBuyRate());
                ciftpTransInfo.setExchangeRate(exchangeRate);

            }

            if (Constant.CURRENCY_TYPE_USD.equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                    BigDecimal.valueOf(Double.valueOf(rate.getSellRate())), 0, RoundingMode.HALF_UP);
                
                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount)).multiply(exchangeRate).setScale(0, roundingMode);
                ciftpTransInfo.setRate(rate.getSellRate());
                ciftpTransInfo.setExchangeRate(exchangeRate);

            }
        }

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

    // private BigDecimal calculateMoneyAtExchangeRate(BigDecimal rate, String currency, String amount, RoundingMode
    // roundingMode) {
    // if (Constant.CURRENCY_TYPE_KHR.equals(currency)) {
    // return rate.multiply(new BigDecimal(amount)).setScale(2, roundingMode);
    // } else if (Constant.CURRENCY_TYPE_USD.equals(currency)) {
    // return new BigDecimal(amount).divide(rate, 0, roundingMode);
    // }
    // return BigDecimal.ZERO;
    // }

    private Result processLimit(String provider, LimitTransInput limitTransInput, CustInfo custInfo,
                                          String requestId, AccountBase accountInfo, TransInfo ciftpTransInfo, CiftpChannelLimit channelLimt) {
        Result result = Result.OK;
        ExecuteT24Output<LimitTransOutput> limit =
                callFundsTransferService.getLimitTrans(provider, limitTransInput, custInfo.getId(), requestId);
        if (Constant.CALL_MICROSERVICE_SUCCESS.equals(limit.getStatus())) {

            BigDecimal creditAmount = new BigDecimal(ciftpTransInfo.getCreditAmount());

            if (new BigDecimal(limit.getData().getMinTrxAmount()).compareTo(creditAmount) == 1) {
                result = new SimpleResult(MBCResponseCode.CIFTP_VALID_LMT_TRANS_MIN.getDesc(), false,
                        MBCResponseCode.CIFTP_VALID_LMT_TRANS_MIN.getCode());
                return result;
            }

            if(Objects.nonNull(limit.getData().getMaxDayAmount())
                    && Double.valueOf(limit.getData().getMaxDayAmount()) >  Double.valueOf(limit.getData().getMaxTrxAmount()))
            {
                if (new BigDecimal(limit.getData().getMaxTrxAmount()).compareTo(creditAmount) == -1) {
                    result = new SimpleResult(limit.getData().getMaxTrxAmount(), false,
                            MBCResponseCode.TRANSACTION_VALID_LMT_MAX.getCode());
                    return result;
                }

                if(new BigDecimal(limit.getData().getMaxDayAmount())
                        .compareTo(new BigDecimal(ciftpTransInfo.getCreditAmount())) == -1) {

                    result = new SimpleResult(MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getDesc(), false,
                            MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getCode());
                    return result;
                }

            }else {
                if(new BigDecimal(limit.getData().getMaxDayAmount())
                        .compareTo(new BigDecimal(ciftpTransInfo.getCreditAmount())) == -1) {

                    result = new SimpleResult(MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getDesc(), false,
                            MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getCode());
                    return result;
                }
            }
        }
        else {
            result = new SimpleResult(limit.getErrorInfo().getErrorDesc(), false, limit.getErrorInfo().getErrorCode());
        }
        return result;
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
