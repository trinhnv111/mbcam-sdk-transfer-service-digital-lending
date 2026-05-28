package com.mbc.mobileapp.command.saving.open;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.*;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;

import com.mbc.common.repository.ComTransDtlSavingRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.services.il.nonsavingacct.PostingRestrict;
import com.mbc.common.services.il.nonsavingacct.ProductInfo;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;

import com.mbc.mobileapp.config.SavingFixedDepositConfig;
import com.mbc.mobileapp.config.SavingFlexiDepositConfig;
import com.mbc.mobileapp.config.SavingRealTimeDepositConfig;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.constant.SavingDepositConstant;
import com.mbc.mobileapp.dto.ProductSavingDto;
import com.mbc.mobileapp.repository.ComProductSavingRepoExtd;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.saving.open.SavingInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
public class DoValidateOpenSaving implements Command {

    @Autowired
    private ApiCustomer apiCustomer;

    @Autowired
    private ComTransRepo comTransRepo;

    @Autowired
    private ComTransDtlSavingRepo comTransDtlSavingRepo;

    @Autowired
    private ComTransProcessRepo comTransProcessRepo;

    @Autowired
    private ComProductSavingRepoExtd comProductSavingRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        //get thÃ´ng tin nationality Ä‘á»ƒ check
//        String nationality = customer.getNationalId();
        SavingInfo savingInfo = request.getSavingInfo();
        try {
            String[] types = {"1", "2", "3"};
            if (!Arrays.asList(types).contains(savingInfo.getMaturityInstruction())) {
                result = new SimpleResult(MBCResponseCode.MATURITY_INSTRUCTION.getDesc(), false,
                        MBCResponseCode.MATURITY_INSTRUCTION.getCode());
                context.setResult(result);
                return !result.isOk();
            }

//            if (!"FULL.KYC".equals(customer.getKycStatus()) && !"KH".equals(nationality)) {
//                result = new SimpleResult(MBCResponseCode.NATIONALITY_INVALID.getDesc(), false,
//                        MBCResponseCode.NATIONALITY_INVALID.getErrorCode());
//                context.setResult(result);
//                return !result.isOk();
//            }

            if (!SavingDepositConstant.KYC_STATUS_ALLOW_SAVING_DEPOSIT.contains(customer.getKycStatus())) {
                result = new SimpleResult(MBCResponseCode.NATIONALITY_INVALID.getDesc(), false,
                        MBCResponseCode.NATIONALITY_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if (!Utility.isValidAmount(savingInfo.getAmount())) {
                result = new SimpleResult(MBCResponseCode.AMOUNT_INVALID.getDesc(), false,
                        MBCResponseCode.AMOUNT_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            //get config value prouduct saving
            List<ProductSavingDto> lstProductSaving = comProductSavingRepo.findByProductCode(request.getSavingProductCode());
            if (CollectionUtils.isEmpty(lstProductSaving)) {
                log.info("[DoValidateOpenSavingFixedDeposit ] config value productCode {} not found", request.getSavingProductCode());
                result = new SimpleResult(MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getDesc(), false,
                        MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }else{
                ProductSavingDto productSavingDto = lstProductSaving.get(0);
                if (productSavingDto.getCurrencyAvailable().indexOf(savingInfo.getCurrency()) == -1){
//                    log.info("[Validate Open Saving] requestId: {}, data: Min Amount Invalid - {} USD", request.getRequestId(), savingInfo.getAmount());
                    result = new SimpleResult(MBCResponseCode.CURRENCY_DEPOSIT_NOT_SUPPORT.getDesc(), false,
                            MBCResponseCode.CURRENCY_DEPOSIT_NOT_SUPPORT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }


                if(Constant.CURRENCY_TYPE_USD.equals(savingInfo.getCurrency())){
                    if (Double.valueOf(savingInfo.getAmount()) < productSavingDto.getAmtMinDepositUsd().doubleValue()){
                        log.info("[Validate Open Saving] requestId: {}, data: Min Amount Invalid - {} USD", request.getRequestId(), savingInfo.getAmount());
                        result = new SimpleResult(MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getDesc(), false,
                                MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }
                }

                if(Constant.CURRENCY_TYPE_KHR.equals(savingInfo.getCurrency())){
                    if (Double.valueOf(savingInfo.getAmount()) < productSavingDto.getAmtMinDepositKhr().doubleValue()){
                        log.info("[Validate Open Saving] requestId: {}, data: Min Amount Invalid - {} KHR", request.getRequestId(), savingInfo.getAmount());

                        result = new SimpleResult(MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getDesc(), false,
                                MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }
                }
            }

            NonSavingAcctInput inputMessage = new NonSavingAcctInput();
            inputMessage.setCustomerId(customer.getHostCifId());
            ExecuteT24Output<List<AccountBase>> nonAccOutput =
                    apiCustomer.getNonSavingAccountList(inputMessage, customer.getId(), request.getRequestId());
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(nonAccOutput.getStatus())) {

                String[] postingRestrict = {"2", "3"};
                String[] postingRestrictDebit = {"1", "3"};
                if (nonAccOutput.getData().size() > 0) {

                    List<AccountBase> lstAccountBases = nonAccOutput.getData();
                    AccountBase debitAccountBase = null;
                    AccountBase beneAccountBase = null;

                    for (AccountBase accountBase : lstAccountBases) {
                        if ((savingInfo.getBeneficiaryAccount().equals(accountBase.getAcctId())) &&
                                savingInfo.getCurrency().equals(accountBase.getAcctnCurrency())) {
                            boolean check = true;
                            for (ProductInfo info : accountBase.getProductInfo()) {
                                if (!"1001".equals(info.getId()) || "719".equals(info.getSubProduct())) {
                                    check = false;
                                }
                            }
                            for (PostingRestrict postingRestrict1 : accountBase.getPostingRestrictList()) {
                                if (Arrays.asList(postingRestrict).contains(postingRestrict1.getId())) {
                                    check = false;
                                }
                            }
                            if (check) {
                                beneAccountBase = accountBase;
                                if (!beneAccountBase.getAcctnCurrency().equals(savingInfo.getCurrency())
                                        || !customer.getHostCifId().equals(beneAccountBase.getCustId())) {
                                    result = new SimpleResult(MBCResponseCode.RECEIVING_ACCOUNT_INVALID.getDesc(), false,
                                            MBCResponseCode.RECEIVING_ACCOUNT_INVALID.getCode());
                                    context.setResult(result);
                                    return !result.isOk();
                                }
                            }
                        }
                        if (savingInfo.getDebitAccount().equals(accountBase.getAcctId())
                                && savingInfo.getCurrency().equals(accountBase.getAcctnCurrency())) {
                            boolean check = true;
                            for (ProductInfo info : accountBase.getProductInfo()) {
                                if ("1001".equals(info.getId()) || "1007".equals(info.getId())) {
                                    check = true;
                                } else {
                                    check = false;
                                }

                                if ("719".equals(info.getSubProduct())) {
                                    check = false;
                                }
                            }
                            for (PostingRestrict postingRestrict1 : accountBase.getPostingRestrictList()) {
                                if (Arrays.asList(postingRestrictDebit).contains(postingRestrict1.getId())) {
                                    check = false;
                                }
                            }
                            if (check) {
                                debitAccountBase = accountBase;
                            }
                        }

                    }

                    if (Objects.nonNull(debitAccountBase)) {
                        if (Double.parseDouble(debitAccountBase.getBalance().getAvailable()) < Double
                                .parseDouble(savingInfo.getAmount())) {
                            result = new SimpleResult(MBCResponseCode.INVALID_AVAILABLE_BALANCE2.getDesc(), false,
                                    MBCResponseCode.INVALID_AVAILABLE_BALANCE2.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }

                    } else {
                        result = new SimpleResult(ResponseCode.DEBIT_ACCOUNT_INCORRECT.getDesc(), false,
                                ResponseCode.DEBIT_ACCOUNT_INCORRECT.getCode());
                        context.setResult(result);
                        return !result.isOk();

                    }

                    if (Objects.isNull(beneAccountBase)) {
                        result = new SimpleResult(MBCResponseCode.RECEIVING_ACCOUNT_INVALID.getDesc(), false,
                                MBCResponseCode.RECEIVING_ACCOUNT_INVALID.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }
                    //estimated interest,tax,amount


                    BigDecimal savingAmount = new BigDecimal(savingInfo.getAmount());
//                    BigDecimal interestRate = new BigDecimal(Objects.nonNull(savingFixedDepositInfo.getCampaignCode())
//                        && Objects.nonNull(savingFixedDepositInfo.getCampaignInterest())
//                        ? String.valueOf(Double.valueOf(savingFixedDepositInfo.getInterestRate())
//                            + Double.valueOf(savingFixedDepositInfo.getCampaignInterest()))
//                            : savingFixedDepositInfo.getInterestRate());

                    BigDecimal interestRate = !Utility.isNull(savingInfo.getCampaignCode())
                            && !Utility.isNull(savingInfo.getCampaignInterest())
                            ? new BigDecimal(savingInfo.getInterestRate())
                            .add(new BigDecimal(savingInfo.getCampaignInterest()))
                            : new BigDecimal(savingInfo.getInterestRate());

                    BigDecimal taxRate = new BigDecimal(savingInfo.getTaxRate());
                    BigDecimal term = null;
                    if (savingInfo.getTerm().endsWith("D")) {
                        term = new BigDecimal(savingInfo.getTerm().replace("D", ""));
                    } else {
                        term = new BigDecimal(savingInfo.getTerm().replace("M", ""));
                    }

                    //USD : 2 -- KHR : 0
                    int scale = Constant.CURRENCY_TYPE_USD.equals(savingInfo.getCurrency()) ? 2 : 0;

                    //LÃ£i dá»± tÃ­nh nháº­n Ä‘Æ°á»£c náº¿u táº¥t toÃ¡n Ä‘Ãºng háº¡n ( = Savings amount * Interest/100  Ä‘Ã£ chá»�n * ( ká»³ háº¡n * 30/360)
                    BigDecimal estimatedInterest = savingAmount
                            .multiply(interestRate.movePointLeft(2)) // /100
                            .multiply(term)
                            .multiply(new BigDecimal(SavingFixedDepositConfig.MONTH))
                            .divide(new BigDecimal(SavingFixedDepositConfig.YEAR), scale, RoundingMode.HALF_UP);

                    // thue du tinh
                    BigDecimal estimatedTax = (taxRate.movePointLeft(2))
                            .multiply(estimatedInterest)
                            .setScale(scale, RoundingMode.HALF_UP);

                    // sá»‘ tiá»�n nháº­n Ä‘Æ°á»£c
                    BigDecimal matureAmount = savingAmount
                            .add(estimatedInterest)
                            .subtract(estimatedTax)
                            .setScale(scale, RoundingMode.HALF_UP);

                    //sá»‘ tiá»�n lÃ£i hÃ ng thÃ¡ng
                    BigDecimal approxNetMonthlyInterest = (estimatedInterest.subtract(estimatedTax))
                            .divide(term, scale, RoundingMode.HALF_UP);

                    savingInfo.setEstimatedInterest(estimatedInterest);
                    savingInfo.setEstimatedTax(estimatedTax);
                    savingInfo.setMatureAmount(matureAmount);
                    savingInfo.setInterestRate(String.valueOf(interestRate));
                    savingInfo.setApproxNetMonthlyInterest(approxNetMonthlyInterest);

                    //create record
                    this.process(customer, response, request, savingInfo, interestRate, lstProductSaving.get(0));
                    if(Constant.SrvcCd.SRVC_SAVING_FIXED_DEPOSIT.equals(request.getSrvcCd())){
                        savingInfo.setSavingType(SavingFixedDepositConfig.SAVING_TYPE);
                    }
                    if(Constant.SrvcCd.SRVC_SAVING_REAL_TIME.equals(request.getSrvcCd())){
                        savingInfo.setSavingType(SavingRealTimeDepositConfig.SAVING_TYPE);
                    }
                    if(Constant.SrvcCd.SRVC_SAVING_FLEXI_TERM.equals(request.getSrvcCd())){
                        savingInfo.setSavingType(SavingFlexiDepositConfig.SAVING_TYPE);
                    }

                    request.setSavingInfo(savingInfo);
                    context.setResponse(response);
                    context.setRequest(request);
                    context.setResult(result);
                    return !result.isOk();
                } else {
                    result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                            ResponseCode.TRANSACTION_FAIL.getCode());
                }
            } else {
                result = new SimpleResult(
                        nonAccOutput.getErrorInfo().getErrorDesc() + " - " + nonAccOutput.getErrorInfo().getErrorDetail(),
                        false, nonAccOutput.getErrorInfo().getErrorCode());
            }

        } catch (Exception e) {
            AppLog.error("[SDK Exception Validate Open Saving] requestId: "+request.getRequestId()+" desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }

    private void process(CustInfo customer, CommonServiceResponse response, CommonServiceRequest request, SavingInfo savingFixedDepositInfo, BigDecimal interestRate, ProductSavingDto productSavingConfig) {
        ComTrans comTrans = new ComTrans();
        comTrans.setCustId(customer.getId());
        comTrans.setDebitAcctName(customer.getNm());
        comTrans.setDebitAcctNo(savingFixedDepositInfo.getDebitAccount());
        comTrans.setDebitCurrency(savingFixedDepositInfo.getCurrency());
        comTrans.setSrvcCd(request.getSrvcCd());
        comTrans.setDebitAmount(BigDecimal.valueOf(Double.parseDouble(savingFixedDepositInfo.getAmount())));
        comTrans.setBranchCode(savingFixedDepositInfo.getBranchCode());
        comTrans.setCreatedBy(customer.getUserId());
        comTrans.setStatus(Constant.COM_STATUS_INT);
        comTrans.setSessionId(request.getSessionId());
        comTrans.setChannel(CommonServiceConstant.CHANNEL_SAVING.get(request.getPartnerSdk()));
        comTrans.setPartnerCode(request.getPartnerSdk());
        comTransRepo.saveAndFlush(comTrans);

        ComTransDtlSaving comTransDtlSaving = new ComTransDtlSaving();
        comTransDtlSaving.setId(comTrans.getId());
        comTransDtlSaving.setCustId(customer.getId());
        comTransDtlSaving.setDebitAcctName(customer.getNm());
        comTransDtlSaving.setDebitAcctNo(savingFixedDepositInfo.getDebitAccount());
        comTransDtlSaving.setDebitCurrency(savingFixedDepositInfo.getCurrency());
        comTransDtlSaving.setDebitAmount(new BigDecimal(savingFixedDepositInfo.getAmount()));
        comTransDtlSaving.setBranchCode(savingFixedDepositInfo.getBranchCode());
        comTransDtlSaving.setCreatedBy(customer.getUserId());
        comTransDtlSaving.setStatus(Constant.COM_STATUS_INT);
        comTransDtlSaving.setCategory(productSavingConfig.getCategory());
        comTransDtlSaving.setProductCode(request.getSavingProductCode());
        comTransDtlSaving.setSubCategory(productSavingConfig.getSubProduct());
        comTransDtlSaving.setPeriod(savingFixedDepositInfo.getTerm());
        comTransDtlSaving.setEstimatedInterest(savingFixedDepositInfo.getEstimatedInterest());
        comTransDtlSaving.setEstimatedTax(savingFixedDepositInfo.getEstimatedTax());
        comTransDtlSaving.setMatureAmount(savingFixedDepositInfo.getMatureAmount());
        comTransDtlSaving.setInterestPaymentPeriod(SavingDepositConstant.SavingDepositType.INTEREST_PAYMENT_PERIOD);
        comTransDtlSaving.setCampaignCode(savingFixedDepositInfo.getCampaignCode());
        comTransDtlSaving.setCampaignInterest(savingFixedDepositInfo.getCampaignInterest());
        comTransDtlSaving.setInterestOption("SRVC_SAVING_FIXED_DEPOSIT".equals(productSavingConfig.getGroupId()) ? productSavingConfig.getNameEn() : null );
        comTransDtlSaving.setInterest(interestRate);

        if ("1".equals(savingFixedDepositInfo.getMaturityInstruction())) {
            savingFixedDepositInfo.setMaturityInstruction(SavingDepositConstant.DisburseType.CLOSE_AT_MATURITY.substring(2));
            comTransDtlSaving.setDisburseForm(SavingDepositConstant.DisburseType.CLOSE_AT_MATURITY);
        } else if ("2".equals(savingFixedDepositInfo.getMaturityInstruction())) {
            savingFixedDepositInfo.setMaturityInstruction(SavingDepositConstant.DisburseType.PRINCIPAL_AND_INTEREST_ROLLOVER.substring(2));
            comTransDtlSaving.setDisburseForm(SavingDepositConstant.DisburseType.PRINCIPAL_AND_INTEREST_ROLLOVER);
        } else {
            savingFixedDepositInfo.setMaturityInstruction(SavingDepositConstant.DisburseType.PRINCIPAL_ROLLOVER.substring(2));
            comTransDtlSaving.setDisburseForm(SavingDepositConstant.DisburseType.PRINCIPAL_ROLLOVER);
        }
        comTransDtlSaving.setReferrerName(StringUtils.isNotEmpty(savingFixedDepositInfo.getReferrerName()) ? savingFixedDepositInfo.getReferrerName() : null);
        comTransDtlSaving.setReferrerPhone(StringUtils.isNotEmpty(savingFixedDepositInfo.getReferrerPhoneNumber()) ? savingFixedDepositInfo.getReferrerPhoneNumber() : null);
        comTransDtlSaving.setReferrerCif(savingFixedDepositInfo.getReferrerCif());

        comTransDtlSaving.setBeneAccount(savingFixedDepositInfo.getBeneficiaryAccount());
        comTransDtlSaving.setSavingType(SavingDepositConstant.SavingType.SAVING_TYPE_NEW);

        //bo sung them truong 07/05/2024
        comTransDtlSaving.setRmCode(savingFixedDepositInfo.getRmCode());
//        comTransDtlSaving.setPartnerCode(savingFixedDepositInfo.getPartnerCode());
        comTransDtlSaving.setPartnerCode(request.getPartnerSdk());

        comTransDtlSavingRepo.saveAndFlush(comTransDtlSaving);

        // TAO BAN GHI COM_TRANS_PROCESS
        ComTransProcess comTransProcess = new ComTransProcess();
        comTransProcess.setStatus(Constant.COM_STATUS_INT);
        comTransProcess.setTransId(comTrans.getId());
        comTransProcess.setSrvcCd(comTrans.getSrvcCd());
        comTransProcessRepo.saveAndFlush(comTransProcess);

        response.setTransId(comTrans.getId());
        response.setTransTime(String.valueOf(comTrans.getCreatedDt().getTime()));
        RedisServer.saveCacheRedis(comTrans.getId(), String.valueOf(comTrans.getCreatedDt().getTime()), 5);
    }
}
