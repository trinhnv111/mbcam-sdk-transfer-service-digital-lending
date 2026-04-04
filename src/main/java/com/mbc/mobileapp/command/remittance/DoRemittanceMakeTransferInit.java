package com.mbc.mobileapp.command.remittance;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.dto.District;
import com.mbc.common.dto.Province;
import com.mbc.common.dto.Ward;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlInternational;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.entity.Cust;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.*;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.RedisServer;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallRemittanceService;
import com.mbc.mobileapp.api.model.remittance.input.init.BeneInfo;
import com.mbc.mobileapp.api.model.remittance.input.init.RemittanceMakeTransferInitInput;
import com.mbc.mobileapp.api.model.remittance.input.init.Remitter;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceBankListOutput;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceDiscount;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceMakeTransferInitOutput;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.repository.CustRepoExtend;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.remittance.init.InitMakeTransferInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class DoRemittanceMakeTransferInit implements Command {
    private final static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final CallRemittanceService apiRemittance;

    private final ComTransRepo comTransRepo;

    private final ComTransProcessRepo comTransProcessRepo;

    private final ComTransDtlInternationalRepo comTransDtlInternationalRepo;
    
    private final CustRepoExtend custRepoExtend;
    
    private final ComWardRepo comWardRepo;
    
    private final ComDistrictRepo comDistrictRepo;
    
    private final ComProvinceRepo comProvinceRepo;

    @Value("${remittance.promo.code}")
    private String promoCode;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result;
        String custId = context.getCustomer().getId();
        String requestId = context.getRequest().getRequestId();
        CustInfo custInfo = context.getCustomer();
        AccountBase accountInfo = (AccountBase) context.getVar(Constant.KeyVar.DEBIT_ACCOUNT_INFO);
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        InitMakeTransferInfo initMakeTransferInfo = request.getInitMakeTransferInfo();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String debitAmount = initMakeTransferInfo.getAmount();
        initMakeTransferInfo.setDebitCurrency(accountInfo.getAcctnCurrency());

        try {
            initMakeTransferInfo.setDescription(convertDescription(initMakeTransferInfo.getDescription()));
            List<String> lstPromoCode = Arrays.asList(promoCode.split(","));

            //fix pentest
            if("970422".equals(initMakeTransferInfo.getDestBankCode())){
                initMakeTransferInfo.setBenAddress("");
            }

            //fix pentest
            if(Constant.CURRENCY_TYPE_KHR.equals(initMakeTransferInfo.getCurrency())){
                result = new SimpleResult(MBCResponseCode.CURRENCY_INVALID.getDesc(), false,
                        MBCResponseCode.CURRENCY_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if(!Utility.isNull(initMakeTransferInfo.getPromoCode())) {
                if(lstPromoCode.indexOf(initMakeTransferInfo.getPromoCode()) == -1) {
                    result = new SimpleResult(MBCResponseCode.REMITTANCE_PROMO_CODE_NOT_EXIST.getDesc(), false, MBCResponseCode.REMITTANCE_PROMO_CODE_NOT_EXIST.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
            }
            
            //check ten bank nhan
            List<RemittanceBankListOutput> bankList = response.getRemittanceBankListOutputList();
            if(!bankList.isEmpty()) {
                for (RemittanceBankListOutput remittanceBankListOutput : bankList) {
                    if(remittanceBankListOutput.getBankCode().equals(initMakeTransferInfo.getDestBankCode())) {
                        if(!remittanceBankListOutput.getBankShortName().equals(initMakeTransferInfo.getDestBankName())) {
                            result = new SimpleResult(MBCResponseCode.BENE_BANK_INFO_INCORRECT.getDesc(), false, MBCResponseCode.BENE_BANK_INFO_INCORRECT.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }else {
                            break;
                        }
                    }
                }
               
            }

            String[] purposes = {"01", "02", "03"};
            if (!Arrays.asList(purposes).contains(initMakeTransferInfo.getPurpose())) {
                result = new SimpleResult(MBCResponseCode.PURPOSE_INVALID.getDesc(), false, MBCResponseCode.PURPOSE_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            String beneDocType = "passport".toUpperCase(Locale.ROOT).equals(custInfo.getIdTypType().toUpperCase(Locale.ROOT)) ? "PASSPORT" : "NATIONAL_ID";

            //check  MBBANK creditCurrency
            if ("970422".equals(initMakeTransferInfo.getDestBankCode())) {
                if (!(Constant.CURRENCY_TYPE_USD.equals(initMakeTransferInfo.getCreditCurrency()) ||
                        Constant.CURRENCY_VND.equals(initMakeTransferInfo.getCreditCurrency()))) {
                    result = new SimpleResult(MBCResponseCode.MB_BANK_CURRENCY.getDesc(), false, MBCResponseCode.MB_BANK_CURRENCY.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
            } else {
                //check other bank creditCurrency
                if (!Constant.CURRENCY_VND.equals(initMakeTransferInfo.getCreditCurrency())) {
                    result = new SimpleResult(MBCResponseCode.OTHER_BANK_CURRENCY.getDesc(), false, MBCResponseCode.OTHER_BANK_CURRENCY.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
            }

            if (!Utility.isValidAmount(debitAmount)) {
                result = new SimpleResult(MBCResponseCode.AMOUNT_INVALID.getDesc(), false,
                        MBCResponseCode.AMOUNT_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if (!Constant.CURRENCY_TYPE_USD.equals(initMakeTransferInfo.getCurrency()) || !Constant.CURRENCY_TYPE_USD.equals(initMakeTransferInfo.getDebitCurrency())) {
                result = new SimpleResult(MBCResponseCode.CURRENCY_INVALID.getDesc(), false,
                        MBCResponseCode.CURRENCY_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if (Double.parseDouble(debitAmount) < 5) {
                result = new SimpleResult("5", false,
                        MBCResponseCode.VALID_AMOUNT_MIN.getCode());
                context.setResult(result);
                return !result.isOk();
            }


            initMakeTransferInfo.setDebitAmount(debitAmount);


            //input call service remittance
            BeneInfo beneInfo = BeneInfo.builder().
                    beneBank(initMakeTransferInfo.getDestBankCode()).
                    beneBankName(initMakeTransferInfo.getDestBankName()).
                    beneAccount(initMakeTransferInfo.getCreditAcctNo()).
                    beneName(initMakeTransferInfo.getCreditAcctName()).
                    beneAddress(initMakeTransferInfo.getBenAddress()).build();

            Remitter remitter = Remitter.builder().account(initMakeTransferInfo.getDebitAcctNo()).
                    name(initMakeTransferInfo.getDebitAcctName()).
                    phoneNumber(custInfo.getPhoneNo()).
                    documentType(beneDocType).
                    documentNum(custInfo.getIdTypNo()).
                    address(getAddress(custInfo, requestId)).
                    custKycStatus(custInfo.getKycStatus()).build();


            RemittanceMakeTransferInitInput input = RemittanceMakeTransferInitInput.builder().
                    beneInfo(beneInfo).
                    remitter(remitter).
                    remittancePaymentType(CommonServiceConstant.TransferType.TRANSFER.name()).
                    transactionMessage(initMakeTransferInfo.getDescription()).
                    transactionPurpose(initMakeTransferInfo.getPurpose()).
                    debitCurrency(initMakeTransferInfo.getDebitCurrency()).
                    creditCurrency(initMakeTransferInfo.getCreditCurrency()).
                    partnerCode(CommonServiceConstant.Channel.MBC_MOBILE.name()).
                    amount(new BigDecimal(debitAmount)).
                    build();
                    

            ExecuteT24Output<RemittanceMakeTransferInitOutput> output = apiRemittance.init(input, custId, requestId);

            if (Objects.isNull(output)) {
                AppLog.error(" DoRemittanceMakeTransferInit timeout ");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            String requestMessageID;
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                RemittanceMakeTransferInitOutput makeTransferInitOutput = output.getData();
                BigDecimal fee = Objects.nonNull(makeTransferInitOutput.getFeeMBC()) 
                    ? new BigDecimal(makeTransferInitOutput.getFeeMBC())
                    : BigDecimal.ZERO;
                
                if(Objects.nonNull(makeTransferInitOutput.getDiscounts()) && !makeTransferInitOutput.getDiscounts().isEmpty()) {
                    for (RemittanceDiscount discount : makeTransferInitOutput.getDiscounts()) {
                        fee = fee.subtract(new BigDecimal(discount.getDiscountAmount()));
                        
                        initMakeTransferInfo.setDiscountCode(discount.getDiscountCode());
                        initMakeTransferInfo.setDiscountAmount(discount.getDiscountAmount());
                    }
                    if(fee.compareTo(BigDecimal.ZERO) == -1) {
                        fee = BigDecimal.ZERO;
                    }
                }
                
                initMakeTransferInfo.setChargeAmount(fee.toString());
                initMakeTransferInfo.setChargeCurrency(makeTransferInitOutput.getFeeMBCCurrency());
                initMakeTransferInfo.setExchangeRate(makeTransferInitOutput.getRate());
                initMakeTransferInfo.setCreditAmount(makeTransferInitOutput.getBenAmount());
                requestMessageID = makeTransferInitOutput.getRequestMessageId();
                initMakeTransferInfo.setTransactionType("Remittance to Vietnam");

                request.setInitMakeTransferInfo(initMakeTransferInfo);

                //check available balance
                result = processCheckAccount(initMakeTransferInfo, accountInfo);
                if (!result.isOk()) {
                    context.setResult(result);
                    return !result.isOk();
                }


                context.setResult(result);
                context.setRequest(request);
                context.setResponse(response);

                //create record transaction
                createProcessTrans(initMakeTransferInfo, requestMessageID, custInfo, request, response);

                return !result.isOk();
            } else if("4912".equals(output.getSoaErrorCode())){
                result = new SimpleResult(MBCResponseCode.TRANSACTION_REMITTANCE_LIMIT_MAX.getDesc(), false,
                        MBCResponseCode.TRANSACTION_REMITTANCE_LIMIT_MAX.getCode());
                context.setResult(result);
            }
            else if ("4945".equals(output.getSoaErrorCode())) {
                result = new SimpleResult(MBCResponseCode.REMITTANCE_LIMIT_DAY.getDesc(), false, MBCResponseCode.REMITTANCE_LIMIT_DAY.getCode());
                context.setResult(result);
                return !result.isOk();
            } else if ("4946".equals(output.getSoaErrorCode())) {
                result = new SimpleResult(MBCResponseCode.REMITTANCE_LIMIT_YEAR.getDesc(), false, MBCResponseCode.REMITTANCE_LIMIT_YEAR.getCode());
                context.setResult(result);
                return !result.isOk();
            } else if ("203".equals(output.getSoaErrorCode())) {
                result = new SimpleResult(MBCResponseCode.CURRENCY_INVALID.getDesc(), false, MBCResponseCode.CURRENCY_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            } else {
                String errorDesc = output.getErrorInfo().getErrorDesc();
                if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                    errorDesc = output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                }
                result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());

            }
        } catch (Exception e) {
            AppLog.error("ERROR DoRemittanceMakeTransferInit: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }

    private void createProcessTrans(InitMakeTransferInfo initMakeTransferInfo, String requestMessageID, CustInfo customer, CommonServiceRequest request, CommonServiceResponse response) {
        ComTrans transInfo = mapper.convertValue(initMakeTransferInfo, ComTrans.class);
        transInfo.setSessionId(request.getSessionId());
        transInfo.setCustId(customer.getId());
        transInfo.setCreatedBy(customer.getUserId());
        transInfo.setStatus(Constant.COM_STATUS_INT);
        transInfo.setSrvcCd(request.getSrvcCd());
        transInfo.setTransferType(CommonServiceConstant.TransferType.INTERNATIONAL.name());
        transInfo.setChannel(request.getDigitalChannel());
        transInfo.setPartnerCode(request.getPartnerSdk());
        comTransRepo.save(transInfo);

        ComTransDtlInternational transDtlInfo = mapper.convertValue(initMakeTransferInfo, ComTransDtlInternational.class);
        transDtlInfo.setId(transInfo.getId());
        transDtlInfo.setCustId(customer.getId());
        transDtlInfo.setCreatedBy(customer.getUserId());
        transDtlInfo.setStatus(Constant.COM_STATUS_INT);
        transDtlInfo.setOwnerCharge(initMakeTransferInfo.isOwnerCharge() ? "1" : "0");
        transDtlInfo.setDebitAmount(new BigDecimal(initMakeTransferInfo.getDebitAmount()));
        transDtlInfo.setCreditAmount(new BigDecimal(initMakeTransferInfo.getCreditAmount()));
        transDtlInfo.setTransferType(CommonServiceConstant.TransferType.INTERNATIONAL.name());
        if ("01".equals(initMakeTransferInfo.getPurpose())) {
            transDtlInfo.setPurpose(CommonServiceConstant.Purpose.TRANSFER_TO_RELATIVE);
        } else if ("02".equals(initMakeTransferInfo.getPurpose())) {
            transDtlInfo.setPurpose(CommonServiceConstant.Purpose.PAYMENT);
        } else {
            transDtlInfo.setPurpose(CommonServiceConstant.Purpose.PERSONAL_SAVING);
        }
        transDtlInfo.setRequestMessageId(requestMessageID);
        transDtlInfo.setDebitAcctType(CommonServiceConstant.AccountType.ACCOUNT.name());
        transDtlInfo.setCreditAcctType(CommonServiceConstant.AccountType.ACCOUNT.name());
        transDtlInfo.setKycStatus(customer.getKycStatus());
        transDtlInfo.setPromoCode(initMakeTransferInfo.getPromoCode());
//        transDtlInfo.setTransId(transInfo.getId());
        comTransDtlInternationalRepo.saveAndFlush(transDtlInfo);


        // TAO BAN GHI COM_TRANS_PROCESS
        ComTransProcess comTransProcess = new ComTransProcess();
        comTransProcess.setStatus(Constant.COM_STATUS_INT);
        comTransProcess.setTransId(transInfo.getId());
        comTransProcess.setSrvcCd(transInfo.getSrvcCd());
        comTransProcessRepo.saveAndFlush(comTransProcess);

        response.setTransId(transInfo.getId());
        response.setTransTime(String.valueOf(transInfo.getCreatedDt().getTime()));
        RedisServer.saveCacheRedis(transInfo.getId(), String.valueOf(transInfo.getCreatedDt().getTime()), 5);
    }

    private Validator.Result processCheckAccount(InitMakeTransferInfo initMakeTransferInfo, AccountBase accountInfo) {
        Validator.Result result;

        BigDecimal totalDebitAmount = new BigDecimal(initMakeTransferInfo.getDebitAmount()).add(new BigDecimal(initMakeTransferInfo.getChargeAmount()));
        BigDecimal available = new BigDecimal(accountInfo.getBalance().getAvailable());
        if (totalDebitAmount.compareTo(available) <= 0) {
            result = new SimpleResult(ResponseCode.SUCCESS.getDesc(), true,
                    ResponseCode.SUCCESS.getCode());
        } else {
            result = new SimpleResult(MBCResponseCode.INVALID_AVAILABLE_BALANCE2.getDesc(), false,
                    MBCResponseCode.INVALID_AVAILABLE_BALANCE2.getCode());
        }

        return result;
    }
    
    private String getAddress(CustInfo custInfo, String requestId) {
        String address = null;
        try {
            Cust cust = custRepoExtend.getById(custInfo.getId());
            Ward ward = comWardRepo.findByWardCode(cust.getWardCode());
            District district = comDistrictRepo.findByDistrictCode(cust.getDistrictCode());
            Province province = comProvinceRepo.getByProvinceCode(cust.getProvinceCode());
            address = ward.getWardName() + ", " + district.getDistrictName() + ", " + province.getProvinceName();
        }catch(Exception e) {
            AppLog.error("[Exception Get Address Remittance] requestId: " + requestId + " desc: " + e);
            
            address = Objects.nonNull(custInfo.getAddr1()) ? custInfo.getAddr1() + ", Phnom Penh" : "Phnom Penh";
        }
        return address;
    }
    
    private String convertDescription(String description) {
        String regex = "PROPERTY,REAL ESATE,APARTMENT,HOUSE,BAT DONG SAN,MUA NHA,MUA DAT,BAN NHA,BAN DAT,CAP,VON,CHUYEN VON,DAU TU,VAY,UNUSED,PROFIT,CAPITAL,CAPITALS,CONTRIBUTION,CONTRIBUTIONS,CONTRIBUTE,CONTRIBUTES,WITHDRAWN,DISBURSEMENT,INVESTMENT,INVESTMENTS,INVEST,LOAN,LOANS,FUND,FUNDS,MONEY,SHARES,SHARE,BOND,BONDS,SECURITIES,PRINCIPAL,PRINCIPALS,INTEREST,INTERESTS,GUARANTEES,GUARANTEE,SHAREHOLDER,SHAREHOLDERS,DEBT,DEBTS,EQUITY,DIVIDEND,DIVIDENDS,CO TUC,LAI";
        List<String> data = Arrays.asList(regex.split(","));
        for (String string : data) {
            if(description.contains(string)) {
                description = "Transfer";
            }
        }
        return description;
        
    }
}
