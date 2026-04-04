
package com.mbc.mobileapp.command.transfer;

import com.fasterxml.jackson.databind.DeserializationFeature;
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
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
public class DoValidateTransfer implements Command {

    private final static ObjectMapper mapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private ComLmtPcDtlRepo lmtPcDtlRepo;

    @Autowired
    private ComLmtPcUsageRepo comLmtPcUsageRepo;

    @Autowired
    private ComTransRepo comTransRepo;

    @Autowired
    private ComTransProcessRepo comTransProcessRepo;

    @Autowired
    private ComTransDtlTransferRepo comTransDtlTransferRepo;

    @Autowired
    private ComChPcDtlRepo comChPcDtlRepo;
    
    @Autowired
    private CallILT24Service callILT24Service;
    
    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Result result = Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String pkgService = customer.getSrvcPcCdTmp() != null ? customer.getSrvcPcCdTmp() : customer.getSrvcPcCd();
//        String srvcCd = request.getSrvcCd();
        String srvcCd = Constant.SrvcCd.SRVC_TRANS_INHOUSE;

        TransInfo transInfo = request.getTransInfo();
        transInfo.setTransactionType(CommonServiceConstant.TransactionType.INHOUSE.name());
//        transInfo.setTransactionType("INHOUSE");
        
        BigDecimal debitAmount = null;
        BigDecimal chargeAmount = BigDecimal.ZERO;
        String chargeCode = null;
        RateT24 exchangeRate = null;
        String inputCcy = null;

        try {
            
            AccountBase accountInfo = (AccountBase) context.getVar(Constant.KeyVar.DEBIT_ACCOUNT_INFO);
            transInfo.setDebitAcctName(customer.getNm());
            transInfo.setDebitCurrency(accountInfo.getAcctnCurrency());
            response.setCurrency(accountInfo.getAcctnCurrency());
            
            if (!Utility.isValidAmount(transInfo.getAmount())) {
                result = new SimpleResult(MBCResponseCode.AMOUNT_INVALID.getDesc(), false,
                    MBCResponseCode.AMOUNT_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            
            List<String> lstCCY = Arrays.asList(transInfo.getDebitCurrency(), transInfo.getCreditCurrency());
            if (lstCCY.indexOf(transInfo.getCurrency()) == -1) {
                result = new SimpleResult(MBCResponseCode.CURRENCY_INVALID.getDesc(), false,
                    MBCResponseCode.CURRENCY_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

           
            
            
         // Get exchange rate
            exchangeRate = getExchangeRate(context);
            if (exchangeRate == null) {
                return !result.isOk();
            }

            if (transInfo.getCreditCurrency().equals(accountInfo.getAcctnCurrency())) {
                transInfo.setDebitAmount(transInfo.getAmount());               
                transInfo.setCreditAmount(transInfo.getAmount());
                inputCcy = CommonServiceConstant.AmountType.DEBIT.name();
            }
            else {
                if (transInfo.getCurrency().equals(transInfo.getCreditCurrency())) {
                    transInfo.setCreditAmount(transInfo.getAmount());
                    inputCcy = CommonServiceConstant.AmountType.CREDIT.name();

                    transInfo.setDebitAmount(exchangeRate(transInfo.getAmount(),
                        transInfo.getCurrency(), exchangeRate, transInfo, RoundingMode.UP, inputCcy).toString());

                }
                else if (transInfo.getCurrency().equals(accountInfo.getAcctnCurrency())) {
                    transInfo.setDebitAmount(transInfo.getAmount());
                    inputCcy = CommonServiceConstant.AmountType.DEBIT.name();

                    transInfo.setCreditAmount(exchangeRate(transInfo.getAmount(),
                        transInfo.getCurrency(), exchangeRate, transInfo, RoundingMode.DOWN, inputCcy).toString());

                }
            }
                      
            debitAmount = new BigDecimal(transInfo.getDebitAmount());
            

            ComChPcDtl comChPcDtl = comChPcDtlRepo.getServiceCharge(pkgService, srvcCd, accountInfo.getAcctnCurrency());
            ComLmtPcDtl comLmtPcDtl = lmtPcDtlRepo.getLimitPackage(pkgService, srvcCd, accountInfo.getAcctnCurrency());

            // TINH PHI GIAO DICH
            if(comChPcDtl != null) {
                chargeCode = comChPcDtl.getChPcCd();
                BigDecimal amountChargeTmp = null;
                if (Constant.CHARGE_VALUE_TYPE_MONEY.equals(comChPcDtl.getValTyp())) {
                    amountChargeTmp = comChPcDtl.getValue();
                    chargeAmount = amountChargeTmp;
                }
                else {
                    amountChargeTmp = debitAmount.multiply(comChPcDtl.getValue()).divide(new BigDecimal(100));
                    
                    if (amountChargeTmp.compareTo(comChPcDtl.getMaxValue()) == 1) {
                        chargeAmount = comChPcDtl.getMaxValue();
                    }
                    else if (amountChargeTmp.compareTo(comChPcDtl.getMinValue()) == -1) {
                        chargeAmount = comChPcDtl.getMinValue();
                    }else {
                        chargeAmount = amountChargeTmp;
                    }

                }
                                
                response.setChargeAmount(String.valueOf(chargeAmount));               
            }else {
                chargeAmount = null;
            }
            
            // TINH HAN MUC GIAO DICH
            if(comLmtPcDtl != null) {
                ComLmtPcUsage comLmtPcUsage = comLmtPcUsageRepo.getLmtPcUsageOnDay(
                    customer.getId(), srvcCd, accountInfo.getAcctnCurrency());                                           

                // CHECK HAN MUC GIAO DICH
                BigDecimal amount = BigDecimal.ZERO;
                if (transInfo.isOwnerCharge()) {
                    if(Objects.nonNull(chargeAmount)) {
                        amount = debitAmount.add(chargeAmount);
                    }else {
                        amount = debitAmount;
                    }
                    
                }
                else {
                    amount = debitAmount;
                }

                // KIEM TRA SO DU KHA DUNG
                BigDecimal available = new BigDecimal(accountInfo.getBalance().getAvailable());
                if (amount.compareTo(available) == 1) {
                    result = new SimpleResult(ResponseCode.TRANSACTION_VALID_DEBIT_AMOUNT_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_VALID_DEBIT_AMOUNT_FAIL.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                BigDecimal maxAmtLmt = comLmtPcDtl.getMaxAmtLmt();
                BigDecimal minTrxAmtLmt = comLmtPcDtl.getMinTrxAmtLmt();
                BigDecimal maxTrxAmtLmt = comLmtPcDtl.getMaxTrxAmtLmt();
                BigDecimal maxOcLmt = comLmtPcDtl.getMaxOcLmt();
                BigDecimal amtLmtUsage = BigDecimal.ZERO;
                BigDecimal ocLmtUsage = BigDecimal.ZERO;

                if (comLmtPcUsage != null) {
                    amtLmtUsage = comLmtPcUsage.getAmtLmtUsage();
                    ocLmtUsage = comLmtPcUsage.getOcLmtUsage();
                }

                if (minTrxAmtLmt.compareTo(debitAmount) == 1) {
                    result = new SimpleResult(MBCResponseCode.CIFTP_VALID_LMT_TRANS_MIN.getDesc(), false,
                        MBCResponseCode.CIFTP_VALID_LMT_TRANS_MIN.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                if (maxTrxAmtLmt.compareTo(debitAmount) == -1) {
                    result = new SimpleResult(maxTrxAmtLmt.toString(), false,
                        MBCResponseCode.TRANSACTION_VALID_LMT_MAX.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                BigDecimal trxAmount = debitAmount.add(amtLmtUsage);
                if (maxAmtLmt.compareTo(trxAmount) == -1) {
                    result = new SimpleResult(MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getDesc(), false,
                        MBCResponseCode.TRANSACTION_VALID_LMT_MAX_AMOUNT_DAY.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                BigDecimal trxOc = ocLmtUsage.add(BigDecimal.ONE);
                if (maxOcLmt.compareTo(trxOc) == -1) {
                    result = new SimpleResult(MBCResponseCode.TRANSACTION_VALID_LMT_MAX_PER_DAY.getDesc(), false,
                        MBCResponseCode.TRANSACTION_VALID_LMT_MAX_PER_DAY.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                
                
                // TAO BAN GHI COM_TRANS
                transInfo.setChargeAmount(Objects.nonNull(chargeAmount) ?  chargeAmount.toString() : "0");
                transInfo.setChargeCurrency(accountInfo.getAcctnCurrency());

                ComTrans comTrans = mapper.convertValue(transInfo, ComTrans.class);
                comTrans.setSessionId(request.getSessionId());
                comTrans.setTransactionType(transInfo.getTransactionType().toUpperCase());
                comTrans.setCustId(customer.getId());
                comTrans.setCreatedBy(customer.getUserId());
                comTrans.setStatus(Constant.COM_STATUS_INT);
                comTrans.setSrvcCd(request.getSrvcCd());
                comTrans.setChannel(request.getDigitalChannel());
                comTrans.setTransferType(transInfo.getTransferType());
                comTrans.setPartnerCode(request.getPartnerSdk());
                comTransRepo.save(comTrans);

                // TAO BAN GHI COM_TRANS_DTL_TRANSFER
                ComTransDtlTransfer transDtlInfo = mapper.convertValue(transInfo, ComTransDtlTransfer.class);
                transDtlInfo.setId(comTrans.getId());
                transDtlInfo.setTransactionType(transDtlInfo.getTransactionType().toUpperCase());
                transDtlInfo.setCustId(customer.getId());
                transDtlInfo.setCreatedBy(customer.getUserId());
                transDtlInfo.setStatus(Constant.COM_STATUS_INT);
                transDtlInfo.setAmountType(inputCcy);
                transDtlInfo.setOwnerCharge(transInfo.isOwnerCharge() ? "1" : "0");
//                transDtlInfo.setRate(Objects.nonNull(exchangeRate) ? exchangeRate.toString() : null);
                transDtlInfo.setRate(transInfo.getRate());
                transDtlInfo.setChargeCode(chargeCode);
                transDtlInfo.setDebitChargeAmount(Objects.nonNull(chargeAmount) ?  chargeAmount.toString() : null);
                comTransDtlTransferRepo.saveAndFlush(transDtlInfo);

                // TAO BAN GHI COM_TRANS_PROCESS
                ComTransProcess comTransProcess = new ComTransProcess();
                comTransProcess.setStatus(Constant.COM_STATUS_INT);
                comTransProcess.setTransId(comTrans.getId());
                comTransProcess.setSrvcCd(comTrans.getSrvcCd());
                comTransProcessRepo.saveAndFlush(comTransProcess);

                response.setTransId(comTrans.getId());
                response.setTransTime(String.valueOf(comTrans.getCreatedDt().getTime()));
                RedisServer.saveCacheRedis(comTrans.getId(), String.valueOf(comTrans.getCreatedDt().getTime()), 5);
                request.setTransInfo(transInfo);
                // request.setAmount(String.valueOf(transInfo.getDebitAmount()));
                // request.setChargeAmount(String.valueOf(transDtlInfo.getChargeAmount()));
            }else {
                result = new SimpleResult(MBCResponseCode.LINIT_CURRENCY_IS_NULL_AND_INACTIVE.getDesc(), false,
                    MBCResponseCode.LINIT_CURRENCY_IS_NULL_AND_INACTIVE.getCode());
            }

        }
        catch (Exception e) {
            AppLog.error("[Validate Inhouse Transfer] requestId: "+request.getRequestId()+", data: {} ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setRequest(request);
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
    
    
    public BigDecimal exchangeRate(String amount, String currency, RateT24 rate, TransInfo transInfo,
        RoundingMode roundingMode, String inputCcy) {

        BigDecimal exchange_amount = BigDecimal.ZERO;

        if (CommonServiceConstant.AmountType.CREDIT.name().equals(inputCcy)) {

            if (Constant.CURRENCY_TYPE_KHR.equals(currency)) {
                
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                    BigDecimal.valueOf(Double.valueOf(rate.getSellRate())), 0, RoundingMode.HALF_UP);
                
                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount))
                    .divide(exchangeRate, 2, roundingMode);
                transInfo.setRate(rate.getSellRate());
                transInfo.setExchangeRate(exchangeRate);

            }

            if (Constant.CURRENCY_TYPE_USD.equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                    BigDecimal.valueOf(Double.valueOf(rate.getBuyRate())), 0, RoundingMode.HALF_UP);
                
                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount)).multiply(exchangeRate).setScale(0, roundingMode);
                transInfo.setRate(rate.getBuyRate());
                transInfo.setExchangeRate(exchangeRate);

            }

        }

        if (CommonServiceConstant.AmountType.DEBIT.name().equals(inputCcy)) {

            if (Constant.CURRENCY_TYPE_KHR.equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                    BigDecimal.valueOf(Double.valueOf(rate.getBuyRate())), 0, RoundingMode.HALF_UP);
                
                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount))
                    .divide(exchangeRate, 2, roundingMode);
                transInfo.setRate(rate.getBuyRate());
                transInfo.setExchangeRate(exchangeRate);

            }

            if (Constant.CURRENCY_TYPE_USD.equals(currency)) {
                BigDecimal exchangeRate = BigDecimal.ONE.divide(
                    BigDecimal.valueOf(Double.valueOf(rate.getSellRate())), 0, RoundingMode.HALF_UP);
                
                exchange_amount = BigDecimal.valueOf(Double.valueOf(amount)).multiply(exchangeRate).setScale(0, roundingMode);
                transInfo.setRate(rate.getSellRate());
                transInfo.setExchangeRate(exchangeRate);

            }
        }

        return exchange_amount;
    }

    
    private RateT24 getExchangeRate(ProcessContext context) throws Exception {
        RateT24 exchangeRate = null;
        Result result = null;
        String dataRate = RedisServer.getCacheRedis(RedisServer.CURRENCY_RATE);
        List<RateT24> lstExchangeRate = new ArrayList<>();
        if (!Utility.isNull(dataRate)) {
            lstExchangeRate = Arrays.asList(JSON.parseObject(dataRate, RateT24[].class));
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
                    String data = JSON.stringify(rateT24.getRateDTOList());
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

}
