
package com.mbc.mobileapp.command.transfer;

import com.mbc.common.api.ApiFundsTransfer;
import com.mbc.common.api.models.fundsTransfer.*;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.constant.TransactionAuthMethod;
import com.mbc.common.entity.*;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.*;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.ChannelEnum;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DoExecuteTransfer implements Command {

    // private final static ObjectMapper mapper =
    // new
    // ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
    // false);

    @Autowired
    private ComTransRepo comTransRepo;

    @Autowired
    private ComTransProcessRepo comTransProcessRepo;

    @Autowired
    private ComTransDtlTransferRepo comTransDtlTransferRepo;

    @Autowired
    private ComLmtPcUsageRepo comLmtPcUsageRepo;

    @Autowired
    private ApiFundsTransfer apiFundsTransfer;

    @Autowired
    private ComLmtPcDtlRepo lmtPcDtlRepo;


    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String custId = customer.getId();
        String requestId = request.getRequestId();
        String transId = request.getTransId();
//        String srvcCd = request.getSrvcCd();
        String srvc_check_limit = Constant.SrvcCd.SRVC_TRANS_INHOUSE;
        String pkgService = customer.getSrvcPcCdTmp() != null ? customer.getSrvcPcCdTmp() : customer.getSrvcPcCd();
        TokenOtp otp = (TokenOtp) context.getVar(Constant.KeyVar.OTP);
        
        try {
            String responseStatus = "";

//            ComTrans infoTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(custId, srvcCd,
//                Constant.COM_STATUS_INT, transId, request.getSessionId());
            ComTrans infoTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionIdAndChannelAndPartnerCode
                    (custId, request.getSrvcCd(), Constant.COM_STATUS_INT, transId, request.getSessionId(), request.getDigitalChannel(), request.getPartnerSdk());

            if(infoTrans != null) {
                ComTransDtlTransfer comTransDtlTransfer = comTransDtlTransferRepo.findById(infoTrans.getId()).get();

                ComLmtPcUsage comLmtPcUsage =
                    comLmtPcUsageRepo.getLmtPcUsageOnDay(custId, srvc_check_limit, infoTrans.getDebitCurrency());
                if (comLmtPcUsage == null) {

                    ComLmtPcDtl comLmtPcDtl =
                        lmtPcDtlRepo.getLimitPackage(pkgService, srvc_check_limit, infoTrans.getDebitCurrency());
                    if(comLmtPcDtl != null) {
                        comLmtPcUsage = new ComLmtPcUsage();
                        comLmtPcUsage.setCcyCd(infoTrans.getDebitCurrency());
                        comLmtPcUsage.setCustId(customer.getId());
                        comLmtPcUsage.setLmtPcCd(comLmtPcDtl.getLmtPcCd());
                        comLmtPcUsage.setMaxAmtLmt(comLmtPcDtl.getMaxAmtLmt());
                        comLmtPcUsage.setMaxOcLmt(comLmtPcDtl.getMaxOcLmt());
                        comLmtPcUsage.setAmtLmtUsage(BigDecimal.ZERO);
                        comLmtPcUsage.setOcLmtUsage(BigDecimal.ZERO);
                        comLmtPcUsage.setMaxTrxAmtLmt(comLmtPcDtl.getMaxTrxAmtLmt());
                        comLmtPcUsage.setMaxTrxCcyCd(infoTrans.getDebitCurrency());
                        comLmtPcUsage.setMinTrxAmtLmt(comLmtPcDtl.getMinTrxAmtLmt());
                        comLmtPcUsage.setMinTrxCcyCd(infoTrans.getDebitCurrency());
                        comLmtPcUsage.setCreatedBy(customer.getUserId());
                        comLmtPcUsage.setSrvcCd(srvc_check_limit);
                        comLmtPcUsage.setVersion(BigDecimal.ZERO);
                    }else {
                        result = new SimpleResult(MBCResponseCode.LINIT_CURRENCY_IS_NULL_AND_INACTIVE.getDesc(), false,
                            MBCResponseCode.LINIT_CURRENCY_IS_NULL_AND_INACTIVE.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }

                }

                request.setAmount(String.valueOf(comTransDtlTransfer.getDebitAmount()));
                request.setChargeAmount(String.valueOf(comTransDtlTransfer.getChargeAmount()));
                context.setRequest(request);
                
                infoTrans.setAuthMethod(Objects.nonNull(otp.getAuthMethod()) ? otp.getAuthMethod() : TransactionAuthMethod.AUTH_METHOD_SMS);
                comTransDtlTransfer.setTransactionDate(new Date());
                ExecuteT24Output<DataOutput> esbOutput = null;
                esbOutput = executeTransfer(infoTrans, comTransDtlTransfer, custId, requestId);          

                if (esbOutput != null) {
                    responseStatus = esbOutput.getStatus();
                    if (Constant.CALL_MICROSERVICE_SUCCESS.equals(responseStatus)) {

                        BigDecimal ocLmtUsage = comLmtPcUsage.getOcLmtUsage();
                        BigDecimal amtLmtUsage = comLmtPcUsage.getAmtLmtUsage();
                        BigDecimal version = comLmtPcUsage.getVersion();
                        version = BigDecimal.ZERO;
                        ocLmtUsage = ocLmtUsage.add(BigDecimal.ONE);
                        amtLmtUsage = amtLmtUsage.add(comTransDtlTransfer.getOwnerCharge().equals("1")
                            ? infoTrans.getDebitAmount().add(comTransDtlTransfer.getChargeAmount())
                            : infoTrans.getDebitAmount());

                        comLmtPcUsage.setVersion(version);
                        comLmtPcUsage.setOcLmtUsage(ocLmtUsage);
                        comLmtPcUsage.setAmtLmtUsage(amtLmtUsage);
                        comLmtPcUsage.setUpdatedBy(customer.getUserId());
                        comLmtPcUsageRepo.saveAndFlush(comLmtPcUsage);

                        response.setFt(esbOutput.getData().getT24Ft());
                        context.setResponse(response);
                        context.putVar(Constant.KeyVar.CURRENCY_TRANSACTION_EXECUTE, infoTrans);

                    }
                    else {
                        String errorDesc = esbOutput.getErrorInfo().getErrorDesc();
                        if (!Utility.isNull(esbOutput.getErrorInfo().getErrorDetail())) {
                            errorDesc =
                                esbOutput.getErrorInfo().getErrorDesc() + " - " + esbOutput.getErrorInfo().getErrorDetail();
                        }
                        result = new SimpleResult(errorDesc, false, esbOutput.getErrorInfo().getErrorCode());
                    }
                }else {
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                        ResponseCode.REQUEST_TIMEOUT.getCode());
                }
            }else {
                result = new SimpleResult(ResponseCode.TRANSACTION_ID_INCORRECT.getDesc(), false,
                    ResponseCode.TRANSACTION_ID_INCORRECT.getCode());
            }

        }
        catch (Exception e) {
            log.error("[Exception Make Inhouse Transfer] requestId: {}, data: {} ", requestId, e.getStackTrace());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();

    }

    private ExecuteT24Output<DataOutput> executeTransfer(ComTrans infoTrans, ComTransDtlTransfer transDtlTransfer,
        String custId, String requestId) throws Exception {        

        FundsTransferInfo transferInfo = new FundsTransferInfo();
        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setAccountName(transDtlTransfer.getCreditAcctName());
        creditAccount.setAccountNumber(transDtlTransfer.getCreditAcctNo());
        creditAccount.setAccountCurrency(transDtlTransfer.getCreditCurrency());
        creditAccount.setAccountType("ACCOUNT");

        DebitAccount debitAccount = new DebitAccount();
        debitAccount.setAccountNumber(transDtlTransfer.getDebitAcctNo());
        debitAccount.setAccountName(transDtlTransfer.getDebitAcctName());
        debitAccount.setAccountCurrency(transDtlTransfer.getDebitCurrency());
        debitAccount.setAccountType("ACCOUNT");

        Amount amount = new Amount();
        
        if(CommonServiceConstant.AmountType.DEBIT.name().equals(transDtlTransfer.getAmountType())) {
            amount.setAmount(transDtlTransfer.getDebitAmount());
            amount.setCurrency(transDtlTransfer.getDebitCurrency());
        }
        
        if(CommonServiceConstant.AmountType.CREDIT.name().equals(transDtlTransfer.getAmountType())) {
            amount.setBenAmount(transDtlTransfer.getCreditAmount());
            amount.setBenCurrency(transDtlTransfer.getCreditCurrency());
        }

        CreditBank creditBank = new CreditBank();
       
        transferInfo.setAmount(amount);
        transferInfo.setBranchCode(transDtlTransfer.getBranchCode());
//        transferInfo.setChargeInfo(null);
        transferInfo.setCreditAccount(creditAccount);
        transferInfo.setCreditBank(creditBank);
        transferInfo.setDebitAccount(debitAccount);
        transferInfo.setRemark(infoTrans.getDescription());
        transferInfo.setTransferType(transDtlTransfer.getTransactionType());  
        
        ChargeInfo chargeInfo = null;
        if(Objects.nonNull(transDtlTransfer.getChargeCode())) {
            chargeInfo = new ChargeInfo();
            ChargeAccount chargeAccount = new ChargeAccount();
            chargeAccount.setAccountCurrency(transDtlTransfer.getDebitCurrency());
            chargeAccount.setAccountName(transDtlTransfer.getDebitAcctName());
            chargeAccount.setAccountNumber(transDtlTransfer.getDebitAcctNo());
            chargeAccount.setAccountType(CommonServiceConstant.AccountType.ACCOUNT.name());
            chargeInfo.setChargeAccount(chargeAccount);
            
//            List<ComTransDomesticCharge> domesticCharge = comTransDomesticChargeRepo.findByTransId(transDtlTransfer.getId());
//            List<ChargeList> cList = new ArrayList<>();
//            for (ComTransDomesticCharge charge : domesticCharge) {
//                ChargeList chargeList = new ChargeList();
//                chargeList.setAmount(transDtlTransfer.getDebitChargeAmount());
//                chargeList.setCode(charge.getChargeCode());
//                chargeList.setCurrency(transDtlTransfer.getDebitCurrency());
//                cList.add(chargeList);
//            }
            
            List<ChargeList> cList = new ArrayList<>();
            ChargeList chargeList = new ChargeList();
            chargeList.setAmount(transDtlTransfer.getDebitChargeAmount());
            chargeList.setCode(transDtlTransfer.getChargeCode());
            chargeList.setCurrency(transDtlTransfer.getDebitCurrency());
            cList.add(chargeList);
            chargeInfo.setChargeList(cList);
        }
        transferInfo.setChargeInfo(Objects.nonNull(chargeInfo) ? chargeInfo : null);
        
        
        AddInfoList addInfoList = new AddInfoList();
        addInfoList.setName("ORDERING_BANK");
        addInfoList.setValue("MBC");
        List<AddInfoList> listAddInfo = new ArrayList<AddInfoList>();
        listAddInfo.add(addInfoList);
        transferInfo.setAddInfoList(listAddInfo);
        
        if(Objects.nonNull(transDtlTransfer.getExchangeRate())) {
            AddInfoList addInfoListRate = new AddInfoList();
            addInfoListRate.setName("TREASURY.RATE");            
            addInfoListRate.setValue(transDtlTransfer.getExchangeRate().toString());
            listAddInfo.add(addInfoListRate);
            
            if(CommonServiceConstant.AmountType.CREDIT.name().equals(transDtlTransfer.getAmountType())) {
                
                AddInfoList addInfoListRoundType = new AddInfoList();
                addInfoListRoundType.setName("ROUND.TYPE");            
                addInfoListRoundType.setValue("EB.UP");
                listAddInfo.add(addInfoListRoundType);
                
            }
            
            if(CommonServiceConstant.AmountType.DEBIT.name().equals(transDtlTransfer.getAmountType())) {
                AddInfoList addInfoListRoundType = new AddInfoList();
                addInfoListRoundType.setName("ROUND.TYPE");            
                addInfoListRoundType.setValue("DOWN");
                listAddInfo.add(addInfoListRoundType);
            }
        }
        
        AddInfoList addInfoListCCY = new AddInfoList();
        addInfoListCCY.setName("AMOUNT_TYPE");
        addInfoListCCY.setValue(transDtlTransfer.getAmountType());
        listAddInfo.add(addInfoListCCY);

        transferInfo.setChannel(ChannelEnum.SDK_RETAIL.getCode());

        ExecuteT24Output<DataOutput> esbOutput = apiFundsTransfer.makeTransfer(transferInfo, custId, requestId, infoTrans.getId());
        if(esbOutput != null) {
         // ====== hard code error start =======
//             esbOutput.setSoaErrorCode("002");
//             esbOutput.setSoaErrorDesc("Request Timeout");
//             ErrorInfo errorInfo = new ErrorInfo();
//             errorInfo.setErrorCode("002");
//             errorInfo.setErrorDesc("Request Timeout");
//             esbOutput.setErrorInfo(errorInfo);
//             esbOutput.setStatus("400");
            // ====== hard code error end =======
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(esbOutput.getStatus())) {
                infoTrans.setFt(esbOutput.getData().getT24Ft());
                infoTrans.setStatus(Constant.COM_STATUS_COM);
                comTransRepo.saveAndFlush(infoTrans);

                transDtlTransfer.setFt(esbOutput.getData().getT24Ft());
                transDtlTransfer.setStatus(Constant.COM_STATUS_COM);
                transDtlTransfer.setRequestId(requestId);
                comTransDtlTransferRepo.saveAndFlush(transDtlTransfer);

                ComTransProcess comTransProcess = new ComTransProcess();
                comTransProcess.setFt(esbOutput.getData().getT24Ft());
                comTransProcess.setSrvcCd(infoTrans.getSrvcCd());
                comTransProcess.setTransId(infoTrans.getId());
                comTransProcess.setStatus(Constant.COM_STATUS_COM);
                comTransProcess.setErrorCode(esbOutput.getErrorInfo().getErrorCode());
                comTransProcess.setErrorDesc(esbOutput.getErrorInfo().getErrorDesc());
                comTransProcessRepo.saveAndFlush(comTransProcess);
                
            } else if ("002".equals(esbOutput.getSoaErrorCode())) {
                infoTrans.setStatus(Constant.COM_STATUS_PND);
                comTransRepo.saveAndFlush(infoTrans);

                transDtlTransfer.setRequestId(requestId);
                transDtlTransfer.setStatus(Constant.COM_STATUS_PND);
                comTransDtlTransferRepo.saveAndFlush(transDtlTransfer);

                ComTransProcess comTransProcess = new ComTransProcess();
                comTransProcess.setSrvcCd(infoTrans.getSrvcCd());
                comTransProcess.setTransId(infoTrans.getId());
                comTransProcess.setStatus(Constant.COM_STATUS_PND);           
                comTransProcessRepo.saveAndFlush(comTransProcess);
            }
            else {
                
                infoTrans.setStatus(Constant.COM_STATUS_FAIL);
                comTransRepo.saveAndFlush(infoTrans);

                transDtlTransfer.setRequestId(requestId);
                transDtlTransfer.setStatus(Constant.COM_STATUS_FAIL);
                comTransDtlTransferRepo.saveAndFlush(transDtlTransfer);

                ComTransProcess comTransProcess = new ComTransProcess();
                comTransProcess.setSrvcCd(infoTrans.getSrvcCd());
                comTransProcess.setTransId(infoTrans.getId());
                comTransProcess.setStatus(Constant.COM_STATUS_FAIL);
                comTransProcess.setErrorCode(esbOutput.getErrorInfo().getErrorCode());
                comTransProcess.setErrorDesc(esbOutput.getErrorInfo().getErrorDesc());
                comTransProcessRepo.saveAndFlush(comTransProcess);
            }
        }else {
            infoTrans.setStatus(Constant.COM_STATUS_PND);
            comTransRepo.saveAndFlush(infoTrans);

            transDtlTransfer.setRequestId(requestId);
            transDtlTransfer.setStatus(Constant.COM_STATUS_PND);
            comTransDtlTransferRepo.saveAndFlush(transDtlTransfer);

            ComTransProcess comTransProcess = new ComTransProcess();
            comTransProcess.setSrvcCd(infoTrans.getSrvcCd());
            comTransProcess.setTransId(infoTrans.getId());
            comTransProcess.setStatus(Constant.COM_STATUS_PND);           
            comTransProcessRepo.saveAndFlush(comTransProcess);
        }
        
        return esbOutput;
    }
}
