package com.mbc.mobileapp.command.saving.topup;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.ComProductSaving;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlSaving;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComProductSavingRepo;
import com.mbc.common.repository.ComTransDtlSavingRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.services.il.customerinfo.CustomerInfoInput;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.services.il.nonsavingacct.ProductInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.DateUtil;
import com.mbc.common.util.RedisServer;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallSavingService;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.api.model.saving.account.detail.GetSavingAccountListInput;
import com.mbc.mobileapp.api.model.saving.interest.InterestInput;
import com.mbc.mobileapp.api.model.saving.interest.InterestOutput;
import com.mbc.mobileapp.api.model.saving.interest.InterestRate;
import com.mbc.mobileapp.command.saving.SavingExchangeRate;
import com.mbc.mobileapp.config.SavingFlexiDepositConfig;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.constant.SavingDepositConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.saving.topup.DebitAccountTopupInfo;
import com.mbc.mobileapp.rest.saving.topup.TopUpSavingInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DoValidateTopUpSaving implements Command {

    private final ApiCustomer apiCustomer;

    private final ComTransRepo comTransRepo;

    private final ComTransDtlSavingRepo comTransDtlSavingRepo;

    private final ComTransProcessRepo comTransProcessRepo;

    private final ComProductSavingRepo comProductSavingRepo;

    private final CallSavingService callSavingService;

    private SavingExchangeRate exchangeRateService = new SavingExchangeRate();


    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();



        // get thông tin nationality để check
        // String nationality = customer.getNationalId();
        AccountBase debitAccountBase = null;
        AccountSaving accountSaving = null;
        TopUpSavingInfo topUpSavingInfo = request.getTopUpSavingInfo();
        try {
            CustomerInfoInput customerInfoInput = new CustomerInfoInput();
            customerInfoInput.setCustomerId(customer.getHostCifId());
            ExecuteT24Output<CustomerInfoT24> cusInfo_Output =
                apiCustomer.getCustomerInfo(customerInfoInput, customer.getId(), request.getRequestId());

            if (Objects.isNull(cusInfo_Output)) {
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                    ResponseCode.REQUEST_TIMEOUT.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(cusInfo_Output.getStatus())) {
                InterestInput input = new InterestInput();
                input.setProductCode(SavingFlexiDepositConfig.PRODUCT_CODE);
                input.setResident("KH".equals(cusInfo_Output.getData().getPerson().getResidence()) ? "Y" : "N");

                ExecuteT24Output<InterestOutput> output =
                        callSavingService.getInterestRate(input, customer.getId(), request.getRequestId());
                if (Objects.isNull(output)) {
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                        ResponseCode.REQUEST_TIMEOUT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                    InterestOutput interestOutput = output.getData();
                    // check amount topup
                    BigDecimal topupAmount = new BigDecimal(topUpSavingInfo.getTopUpAmount());
                    BigDecimal totalAmountDepositKHR = new BigDecimal("0.0");
                    BigDecimal totalAmountDepositUSD = new BigDecimal("0.0");
                    String currencyTopup = topUpSavingInfo.getTopUpCurrency();

                    ComProductSaving comProductSaving =
                        getCurrentProduct(SavingFlexiDepositConfig.PRODUCT_CODE, SavingFlexiDepositConfig.CATEGORY);
                    mappingTotalDepositAmountInfo(customer, request.getRequestId(), interestOutput);

                    for (InterestRate ir : interestOutput.getInterestRate()) {
                        if (Constant.CURRENCY_TYPE_KHR.equalsIgnoreCase(ir.getCurrency())) {
                            totalAmountDepositKHR = ir.getTotalAmountDeposit();
                        }
                        if (Constant.CURRENCY_TYPE_USD.equalsIgnoreCase(ir.getCurrency())) {
                            totalAmountDepositUSD = ir.getTotalAmountDeposit();
                        }
                    }

                    // check for KHR currency
                    if (SavingDepositConstant.KHR.equalsIgnoreCase(currencyTopup)) {
                        if (topupAmount.compareTo(comProductSaving.getAmtMinTopupKhr()) < 0) {
                            result = new SimpleResult(MBCResponseCode.AMOUNT_INVALID.getDesc(), false,
                                MBCResponseCode.AMOUNT_INVALID.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }
                        if (comProductSaving.getAmtMaxNotFullKycKhr().subtract(totalAmountDepositKHR)
                            .compareTo(topupAmount) < 0
                            && !SavingDepositConstant.T24KycStatus.FULL_KYC.equalsIgnoreCase(customer.getKycStatus())) {
                            result = new SimpleResult(
                                String.format(MBCResponseCode.TOPUP_MAX_AMOUNT.getDesc(),
                                    comProductSaving.getAmtMaxNotFullKycKhr(),
                                    comProductSaving.getAmtMaxNotFullKycKhr().subtract(totalAmountDepositKHR)),
                                false, MBCResponseCode.TOPUP_MAX_AMOUNT.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }

                    }

                    if (SavingDepositConstant.USD.equalsIgnoreCase(currencyTopup)) {
                        if (topupAmount.compareTo(comProductSaving.getAmtMinTopupUsd()) < 0) {
                            result = new SimpleResult(MBCResponseCode.AMOUNT_INVALID.getDesc(), false,
                                MBCResponseCode.AMOUNT_INVALID.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }
                        if (comProductSaving.getAmtMaxNotFullKycUsd().subtract(totalAmountDepositUSD)
                            .compareTo(topupAmount) < 0
                            && !SavingDepositConstant.T24KycStatus.FULL_KYC.equalsIgnoreCase(customer.getKycStatus())) {
                            result = new SimpleResult(
                                String.format(MBCResponseCode.TOPUP_MAX_AMOUNT.getDesc(),
                                    comProductSaving.getAmtMaxNotFullKycUsd(),
                                    comProductSaving.getAmtMaxNotFullKycUsd().subtract(totalAmountDepositUSD)),
                                false, MBCResponseCode.TOPUP_MAX_AMOUNT.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }
                    }

                    // check exist debit account

                    NonSavingAcctInput inputMessage = new NonSavingAcctInput();
                    inputMessage.setCustomerId(customer.getHostCifId());
                    ExecuteT24Output<List<AccountBase>> nonAccOutput =
                        apiCustomer.getNonSavingAccountList(inputMessage, customer.getId(), request.getRequestId());

                    if (Constant.CALL_MICROSERVICE_SUCCESS.equals(nonAccOutput.getStatus())) {
                        if (CollectionUtils.isEmpty(nonAccOutput.getData())) {
                            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                                ResponseCode.TRANSACTION_FAIL.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }

                        DebitAccountTopupInfo debitAccountTopupInfo = topUpSavingInfo.getDebitAccount();
                        List<AccountBase> lstAccountBases = nonAccOutput.getData();
                        for (AccountBase accountBase : lstAccountBases) {
                            if (debitAccountTopupInfo.getDebitAccountNumber().equals(accountBase.getAcctId())
                                && debitAccountTopupInfo.getDebitAccountCurrency()
                                    .equals(accountBase.getAcctnCurrency())) {
                                debitAccountBase = accountBase;
                                break;
                            }
                        }

                        if (debitAccountBase != null) {
                            if (Double.parseDouble(debitAccountBase.getBalance().getAvailable()) < Double
                                .parseDouble(topUpSavingInfo.getTopUpAmount())) {
                                result = new SimpleResult(ResponseCode.TRANSACTION_VALID_DEBIT_AMOUNT_FAIL.getDesc(),
                                    false, ResponseCode.TRANSACTION_VALID_DEBIT_AMOUNT_FAIL.getCode());
                                context.setResult(result);
                                return !result.isOk();
                            }

                        }
                        else {
                            result = new SimpleResult(ResponseCode.DEBIT_ACCOUNT_INCORRECT.getDesc(), false,
                                ResponseCode.DEBIT_ACCOUNT_INCORRECT.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }
                    }
                    else {
                        result = new SimpleResult(
                            nonAccOutput.getErrorInfo().getErrorDesc() + " - "
                                + nonAccOutput.getErrorInfo().getErrorDetail(),
                            false, nonAccOutput.getErrorInfo().getErrorCode());
                        context.setResult(result);
                        return !result.isOk();
                    }

                    /// check exist creditor account for topup
                    if (accountSaving == null) {

                        GetSavingAccountListInput message = new GetSavingAccountListInput();
                        message.setCustomerId(null);
                        message.setAccountId(topUpSavingInfo.getSavingId());

                        ExecuteT24Output<List<AccountSaving>> savingAccOutput =
                            callSavingService.getSavingAccountList(message, customer.getId(), request.getRequestId());
                        if (Constant.CALL_MICROSERVICE_SUCCESS.equals(savingAccOutput.getStatus())) {
                            if (savingAccOutput.getData().size() > 0) {
                                List<AccountSaving> lstAccountSaving = savingAccOutput.getData();

                                // check saving account
                                for (AccountSaving saving : lstAccountSaving) {
                                    if (topUpSavingInfo.getSavingId().equals(saving.getAccountNumber())
                                        && topUpSavingInfo.getTopUpCurrency()
                                            .equals(saving.getAccountCurrency())) {
                                        accountSaving = saving;
                                        break;
                                    }
                                }
                                if (accountSaving != null) {

                                    if(!CommonServiceConstant.CHANNEL_SDK_RETAIL.equals(request.getDigitalChannel())){
                                        result = new SimpleResult(ResponseCode.SAVING_ACCOUNT_INCORRECT.getDesc(), false,
                                                ResponseCode.SAVING_ACCOUNT_INCORRECT.getCode());
                                        context.setResult(result);
                                        return !result.isOk();
                                    }
                                    
                                    if(!customer.getHostCifId().equals(accountSaving.getCustomerId())) {
                                        result = new SimpleResult(ResponseCode.SAVING_ACCOUNT_INCORRECT.getDesc(), false,
                                            ResponseCode.SAVING_ACCOUNT_INCORRECT.getCode());
                                        context.setResult(result);
                                        return !result.isOk();
                                    }
                                    
                                    // check saving currency and debit currency is same

                                    if (!debitAccountBase.getAcctnCurrency()
                                        .equalsIgnoreCase(accountSaving.getAccountCurrency())) {
                                        result = new SimpleResult(
                                            MBCResponseCode.TOPUP_DEBIT_CURRENCY_NOT_MATCH.getDesc(), false,
                                            MBCResponseCode.TOPUP_DEBIT_CURRENCY_NOT_MATCH.getCode());
                                    }
                                    else {
                                        
                                        if(Double.valueOf(accountSaving.getTenor().getValue()) >= 12) {
                                         // //Check thoi gian nap tien vao so
                                            String dueDate = accountSaving.getMaturityDate();
                                            SimpleDateFormat format = new SimpleDateFormat(DateUtil.DATE_SIMPLE_REVERSE);
                                            Date date1 = format.parse(dueDate);
                                            String date2str = format.format(new Date());
                                            Date date2 = format.parse(date2str);
                                            Long checkTime = date1.getTime() - date2.getTime();

                                            if ((checkTime
                                                / (1000 * 60 * 60 * 24)) <= SavingFlexiDepositConfig.MATURITY_BACKDATE) {
                                                result = new SimpleResult(
                                                    String.format(
                                                        MBCResponseCode.TOPUP_MATURITY_DATE_NOT_VALID.getDesc(),
                                                        SavingFlexiDepositConfig.MATURITY_BACKDATE),
                                                    false, MBCResponseCode.TOPUP_MATURITY_DATE_NOT_VALID.getCode());
                                            }
                                            else {
                                                process(customer, response, request, topUpSavingInfo,
                                                    debitAccountBase, accountSaving);
                                            }
                                        }else {
                                            process(customer, response, request, topUpSavingInfo,
                                                debitAccountBase, accountSaving);
                                        }
                                    }

                                }
                                else {
                                    result = new SimpleResult(ResponseCode.SAVING_ACCOUNT_INCORRECT.getDesc(), false,
                                        ResponseCode.SAVING_ACCOUNT_INCORRECT.getCode());
                                    context.setResult(result);
                                    return !result.isOk();
                                }
                                //
                            }
                            else {
                                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                                    ResponseCode.TRANSACTION_FAIL.getCode());
                            }

                        }
                        else {
                            result = new SimpleResult(
                                savingAccOutput.getErrorInfo().getErrorDesc() + " - "
                                    + savingAccOutput.getErrorInfo().getErrorDetail(),
                                false, savingAccOutput.getErrorInfo().getErrorCode());
                        }
                    }
                }
                else {
                    result = new SimpleResult(MBCResponseCode.INTEREST_RATE_FAIL.getDesc(), false,
                        MBCResponseCode.INTEREST_RATE_FAIL.getCode());
                }
            }
            else {
                result = new SimpleResult(MBCResponseCode.INTEREST_RATE_FAIL.getDesc(), false,
                    MBCResponseCode.INTEREST_RATE_FAIL.getCode());
            }

        }
        catch (Exception e) {
            AppLog.error("DoValidateTopUpFlexiDeposit sERROR", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }

    private void process(CustInfo customer, CommonServiceResponse response, CommonServiceRequest request,
        TopUpSavingInfo topUpSavingDepositInfo, AccountBase debitAccountBase, AccountSaving accountSaving) {
        // Tao ban ghi COM_TRANS
        ComTrans comTrans = new ComTrans();
        comTrans.setCustId(customer.getId());
        comTrans.setSrvcCd(request.getSrvcCd());

        comTrans.setDebitAcctName(debitAccountBase.getAcctnName());
        comTrans.setDebitAcctNo(debitAccountBase.getAcctId());
        comTrans.setDebitCurrency(topUpSavingDepositInfo.getDebitAccount().getDebitAccountCurrency());
        comTrans.setDebitAmount(BigDecimal.valueOf(Double.valueOf(topUpSavingDepositInfo.getTopUpAmount())));
        comTrans.setAmount(BigDecimal.valueOf(Double.valueOf(topUpSavingDepositInfo.getTopUpAmount())));
        comTrans.setCurrency(topUpSavingDepositInfo.getTopUpCurrency());

        comTrans.setBranchCode(topUpSavingDepositInfo.getBranchCode());
        comTrans.setCreatedBy(customer.getUserId());
        comTrans.setStatus(Constant.COM_STATUS_INT);
        comTrans.setSessionId(request.getSessionId());
        comTrans.setDescription(topUpSavingDepositInfo.getRemark());
        comTrans.setTransactionType(CommonServiceConstant.TransactionType.INHOUSE.name());
        comTrans.setChannel(request.getDigitalChannel());
        comTrans.setPartnerCode(request.getPartnerSdk());
        comTransRepo.saveAndFlush(comTrans);

        // TAO BAN GHI COM_TRAN_DTL_SAVING
        ComTransDtlSaving comTransDtlSaving = new ComTransDtlSaving();
        comTransDtlSaving.setId(comTrans.getId());
        comTransDtlSaving.setCustId(customer.getId());

        comTransDtlSaving.setDebitAcctName(debitAccountBase.getAcctnName());
        comTransDtlSaving.setDebitAcctNo(debitAccountBase.getAcctId());
        comTransDtlSaving.setDebitCurrency(topUpSavingDepositInfo.getDebitAccount().getDebitAccountCurrency());
        comTransDtlSaving.setDebitAmount(BigDecimal.valueOf(Double.valueOf(topUpSavingDepositInfo.getTopUpAmount())));

        comTransDtlSaving.setSavingAcctNo(accountSaving.getAccountNumber());
        comTransDtlSaving
            .setSavingAmount(BigDecimal.valueOf(Double.parseDouble(topUpSavingDepositInfo.getTopUpAmount())));
        comTransDtlSaving.setSavingCurrency(topUpSavingDepositInfo.getTopUpCurrency());
        comTransDtlSaving.setSavingAcctName(accountSaving.getAccountName());

        comTransDtlSaving.setBranchCode(topUpSavingDepositInfo.getBranchCode());
        comTransDtlSaving.setCreatedBy(customer.getUserId());
        comTransDtlSaving.setStatus(Constant.COM_STATUS_INT);
        comTransDtlSaving.setCategory(SavingFlexiDepositConfig.CATEGORY);
        comTransDtlSaving.setProductCode(SavingFlexiDepositConfig.PRODUCT_CODE);
        comTransDtlSaving.setSubCategory(SavingFlexiDepositConfig.SUB_PRODUCT_CODE);
        comTransDtlSaving.setSavingType(CommonServiceConstant.SavingType.SAVING_TYPE_ADMORE);
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

    private void mappingTotalDepositAmountInfo(CustInfo customer, String requestId, InterestOutput interestOutput)
        throws IOException {

        // if (SavingDepositConstant.T24KycStatus.FULL_KYC.equalsIgnoreCase(customer.getKycStatus())) {
        // return;
        // }
        if (CollectionUtils.isEmpty(interestOutput.getInterestRate()))
            return;

        GetSavingAccountListInput message = new GetSavingAccountListInput();
        message.setCustomerId(customer.getHostCifId());

        ExecuteT24Output<List<AccountSaving>> savingAccOutput =
            callSavingService.getSavingAccountListV3(message, customer.getId(), requestId);
        if (Constant.CALL_MICROSERVICE_SUCCESS.equals(savingAccOutput.getStatus())) {
            BigDecimal totalKHR = BigDecimal.ZERO;
            BigDecimal totalUSD = BigDecimal.ZERO;

            for (InterestRate interestRate : interestOutput.getInterestRate()) {
                BigDecimal total =
                    new BigDecimal(calculateTotalDepositAmountByCurrencyAndProductCategoryId(savingAccOutput.getData(),
                        SavingFlexiDepositConfig.CATEGORY, interestRate.getCurrency())).setScale(2,
                            RoundingMode.HALF_UP);
                if (Constant.CURRENCY_TYPE_KHR.equalsIgnoreCase(interestRate.getCurrency())) {
                    totalKHR = total;
                }

                if (Constant.CURRENCY_TYPE_USD.equalsIgnoreCase(interestRate.getCurrency())) {
                    totalUSD = total;
                }
            }
            // convert and set total amount deposit again
            for (InterestRate interestRate : interestOutput.getInterestRate()) {
                if (Constant.CURRENCY_TYPE_KHR.equalsIgnoreCase(interestRate.getCurrency())) {
                    interestRate.setTotalAmountDeposit(exchangeRateService.getTotalKHR(totalKHR, totalUSD));
                }
                if (Constant.CURRENCY_TYPE_USD.equalsIgnoreCase(interestRate.getCurrency())) {
                    interestRate.setTotalAmountDeposit(exchangeRateService.getTotalUSD(totalKHR, totalUSD));
                }
            }
        }
    }

    private boolean isProductByCategoryIdExist(List<ProductInfo> productInfos, String productCategoryId) {
        return productInfos.stream().anyMatch(productInfo -> productInfo.getId().equalsIgnoreCase(productCategoryId));
    }

    private double calculateTotalDepositAmountByCurrencyAndProductCategoryId(List<AccountSaving> accountSavings,
        String productCategoryId, String currency) {
        double total = 0.0;
        if (CollectionUtils.isEmpty(accountSavings))
            return total;

        for (AccountSaving accountSaving : accountSavings) {
            boolean isProductExist =
                isProductByCategoryIdExist(accountSaving.getProductInfo(), SavingFlexiDepositConfig.CATEGORY);
            if (currency.equalsIgnoreCase(accountSaving.getAccountCurrency()) && isProductExist) {
                total += Double.parseDouble(accountSaving.getBalance().getWorking());
            }
        }
        return total;
    }

    private ComProductSaving getCurrentProduct(String productCode, String productCategory) {
        return comProductSavingRepo.findByProductCodeAndCategory(productCode, productCategory)
            .orElseThrow(() -> new RuntimeException("Product saving not found"));
    }
}
