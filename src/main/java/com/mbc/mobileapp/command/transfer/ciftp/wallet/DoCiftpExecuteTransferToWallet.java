
package com.mbc.mobileapp.command.transfer.ciftp.wallet;

import com.mbc.common.api.ApiFundsTransfer;
import com.mbc.common.api.models.fundsTransfer.*;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.constant.TransactionAuthMethod;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDomesticCharge;
import com.mbc.common.entity.ComTransDtlTransfer;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDomesticChargeRepo;
import com.mbc.common.repository.ComTransDtlTransferRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.DateUtil;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.ChannelEnum;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import kh.org.nbc.bakong_khqr.BakongKHQR;
import kh.org.nbc.bakong_khqr.model.KHQRDecodeData;
import kh.org.nbc.bakong_khqr.model.KHQRResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DoCiftpExecuteTransferToWallet implements Command {

    @Autowired
    private ComTransRepo comTransRepo;

    @Autowired
    private ComTransProcessRepo comTransProcessRepo;

    @Autowired
    private ComTransDtlTransferRepo comTransDtlTransferRepo;

    @Autowired
    private ComTransDomesticChargeRepo comTransDomesticChargeRepo;

    @Autowired
    private ApiFundsTransfer apiFundsTransfer;
    
    private final List<String> CHECK_SRVC = Arrays.asList("SRVC_TRANS_CIFTP_WALLET", "SRVC_TRANS_CIFTP_KHQR");

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CustInfo customer = context.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String srvcCd = request.getSrvcCd();
        String transId = request.getTransId();
        String custId = customer.getId();
        String requestId = request.getRequestId();
        TokenOtp otp = (TokenOtp) context.getVar(Constant.KeyVar.OTP);

        try {
            
            if(CHECK_SRVC.indexOf(request.getSrvcCd()) == -1) {
                result = new SimpleResult(ResponseCode.SRVC_INCORRECT.getDesc(), false, ResponseCode.SRVC_INCORRECT.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            
//            ComTrans infoTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(custId, srvcCd,
//                Constant.COM_STATUS_INT, transId, request.getSessionId());

            ComTrans infoTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionIdAndChannelAndPartnerCode
                    (custId, srvcCd, Constant.COM_STATUS_INT, transId, request.getSessionId(), request.getDigitalChannel(), request.getPartnerSdk());
            if (infoTrans != null) {
                ComTransDtlTransfer comTransDtlTransfer =
                    comTransDtlTransferRepo.findByIdAndStatus(infoTrans.getId(), Constant.COM_STATUS_INT);
                if (comTransDtlTransfer != null) {
                    ExecuteT24Output<DataOutput> esbOutput = null;
                    infoTrans.setAuthMethod(Objects.nonNull(otp.getAuthMethod()) ? otp.getAuthMethod() : TransactionAuthMethod.AUTH_METHOD_SMS);
                    esbOutput = executeTransfer(infoTrans, comTransDtlTransfer, custId, requestId);
                    if (esbOutput != null) {
                        if (Constant.CALL_MICROSERVICE_SUCCESS.equals(esbOutput.getStatus())) {
                            response.setFt(esbOutput.getData().getT24Ft());
                            response.setTransHash(esbOutput.getData().getTransHash());
                            context.setResponse(response);
                            context.putVar(Constant.KeyVar.CURRENCY_TRANSACTION_EXECUTE, infoTrans);
                        }
                        else {
                            String errorDesc = esbOutput.getErrorInfo().getErrorDesc();
                            if (!Utility.isNull(esbOutput.getErrorInfo().getErrorDetail())) {
                                errorDesc = esbOutput.getErrorInfo().getErrorDesc() + " - "
                                    + esbOutput.getErrorInfo().getErrorDetail();
                            }
                            result = new SimpleResult(errorDesc, false, esbOutput.getErrorInfo().getErrorCode());
                        }
                    }
                    else {
                        result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());
                    }
                }
                else {
                    result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                        MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
                }
            }
            else {
                result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                    MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
            }
        }
        catch (Exception e) {
            log.error("[Exception execute to wallet] requestId: {} desc: {}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setRequest(request);
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

    private ExecuteT24Output<DataOutput> executeTransfer(ComTrans infoTrans, ComTransDtlTransfer transDtlTransfer,
        String custId, String requestId) throws Exception {

        Date transactionDate = new Date();
        FundsTransferInfo transferInfo = new FundsTransferInfo();
        List<AddInfoList> listAddInfo = new ArrayList<AddInfoList>();

        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setAccountName(transDtlTransfer.getCreditAcctName());
        creditAccount.setAccountNumber(transDtlTransfer.getCreditAcctNo());
        creditAccount.setAccountCurrency(transDtlTransfer.getCreditCurrency());
        creditAccount.setAccountType(transDtlTransfer.getCreditAcctType());

        DebitAccount debitAccount = new DebitAccount();
        debitAccount.setAccountNumber(transDtlTransfer.getDebitAcctNo());
        debitAccount.setAccountName(transDtlTransfer.getDebitAcctName());
        debitAccount.setAccountCurrency(transDtlTransfer.getDebitCurrency());
        debitAccount.setAccountType(transDtlTransfer.getDebitAcctType());
        
        BenAccount benAccount = new BenAccount();
        if (!Utility.isNull(transDtlTransfer.getPayloadQr())) {
            KHQRResponse<KHQRDecodeData> response = BakongKHQR.decode(transDtlTransfer.getPayloadQr());
            
            if(CommonServiceConstant.BakongQRPayType.MERCHANT.name().equals(transDtlTransfer.getQrPayType())) {
                benAccount.setAccountName(response.getData().getMerchantName());                            
                benAccount.setAccountNumber(response.getData().getMerchantId());
                benAccount.setAccountType(CommonServiceConstant.AccountType.ACCOUNT.name());
                benAccount.setAccountCurrency(transDtlTransfer.getCreditCurrency());
                benAccount.setBenAmount(String.valueOf(transDtlTransfer.getCreditAmount()));
                
                creditAccount.setAccountName(response.getData().getAcquiringBank());
            }
            
            if(CommonServiceConstant.BakongQRPayType.REMITTANCE.name().equals(transDtlTransfer.getQrPayType())) {
                benAccount.setAccountName(response.getData().getMerchantName());                            
                benAccount.setAccountNumber(response.getData().getAccountInformation());
                benAccount.setAccountType(CommonServiceConstant.AccountType.ACCOUNT.name());
                benAccount.setAccountCurrency(transDtlTransfer.getCreditCurrency());
                benAccount.setBenAmount(String.valueOf(transDtlTransfer.getCreditAmount()));
                
                creditAccount.setAccountName(response.getData().getAcquiringBank());
            }
            
            if(CommonServiceConstant.BakongQRPayType.SOLO.name().equals(transDtlTransfer.getQrPayType())) {
                benAccount.setAccountName(response.getData().getMerchantName());                            
                benAccount.setAccountNumber(response.getData().getBakongAccountID());
                benAccount.setAccountType(CommonServiceConstant.AccountType.WALLET.name());
                benAccount.setAccountCurrency(transDtlTransfer.getCreditCurrency());
                benAccount.setBenAmount(String.valueOf(transDtlTransfer.getCreditAmount()));
                
                creditAccount.setAccountName(response.getData().getMerchantName());
            }           

        }else {
            benAccount.setAccountName(creditAccount.getAccountName());                            
            benAccount.setAccountNumber(creditAccount.getAccountNumber());
            benAccount.setAccountType(CommonServiceConstant.AccountType.WALLET.name());
            benAccount.setAccountCurrency(creditAccount.getAccountCurrency());
            benAccount.setBenAmount(String.valueOf(transDtlTransfer.getCreditAmount()));
        }

        Amount amount = new Amount();
        amount.setAmount(transDtlTransfer.getDebitAmount());
        amount.setCurrency(transDtlTransfer.getDebitCurrency());
        amount.setBenAmount(transDtlTransfer.getCreditAmount());
        amount.setBenCurrency(transDtlTransfer.getCreditCurrency());
        
//        if(ServiceConstant.AmountType.DEBIT.name().equals(transDtlTransfer.getAmountType())) {
//            amount.setAmount(transDtlTransfer.getDebitAmount());
//            amount.setCurrency(transDtlTransfer.getDebitCurrency());
//        }
//        
//        if (ServiceConstant.AmountType.CREDIT.name().equals(transDtlTransfer.getAmountType())) {
//            amount.setBenAmount(transDtlTransfer.getCreditAmount());
//            amount.setBenCurrency(transDtlTransfer.getCreditCurrency());
//        }

        CreditBank creditBank = new CreditBank();
        creditBank.setCode(transDtlTransfer.getDestBankPartiCode());
        creditBank.setName(transDtlTransfer.getDestBankName());

        ChargeInfo chargeInfo = new ChargeInfo();
        ChargeAccount chargeAccount = new ChargeAccount();
        chargeAccount.setAccountCurrency(transDtlTransfer.getDebitCurrency());
        chargeAccount.setAccountName(transDtlTransfer.getDebitAcctName());
        chargeAccount.setAccountNumber(transDtlTransfer.getDebitAcctNo());
        chargeAccount.setAccountType("ACCOUNT");
        chargeInfo.setChargeAccount(chargeAccount);

        List<ComTransDomesticCharge> domesticCharge =
            comTransDomesticChargeRepo.findByTransId(transDtlTransfer.getId());
        List<ChargeList> lstCList = new ArrayList<ChargeList>();
        for (ComTransDomesticCharge charge : domesticCharge) {
            ChargeList chargeList = new ChargeList();
            chargeList.setAmount(String.valueOf(transDtlTransfer.getDebitChargeAmount()));
            chargeList.setCode(charge.getChargeCode());
            chargeList.setCurrency(transDtlTransfer.getDebitCurrency());
            lstCList.add(chargeList);

        }
        chargeInfo.setChargeList(lstCList);

        AddInfoList addInfoList = new AddInfoList();
        addInfoList.setName("ORDERING_BANK");
        addInfoList.setValue("MBC");

        AddInfoList addInfoListService = new AddInfoList();
        addInfoListService.setName("SERVICE");
        addInfoListService.setValue(transDtlTransfer.getTransferService());

        AddInfoList addInfoListChannel = new AddInfoList();
        addInfoListChannel.setName("CHANNEL");
        addInfoListChannel.setValue(ChannelEnum.SDK_RETAIL.getCode());

        AddInfoList addInfoListOrgData = new AddInfoList();
        addInfoListOrgData.setName("ORG.DATA");
        addInfoListOrgData.setValue("SENDER");

        AddInfoList addInfoListQRPayment = new AddInfoList();
        addInfoListQRPayment.setName("QRPAYMENT");
        addInfoListQRPayment
            .setValue(!Utility.isNull(transDtlTransfer.getPayloadQr()) ? Constant.YES_1 : Constant.NO_1);

        AddInfoList addInfoListQRContent = new AddInfoList();
        addInfoListQRContent.setName("QRCONTENT");
        addInfoListQRContent.setValue(transDtlTransfer.getPayloadQr());

        AddInfoList addInfoListQRPayType = new AddInfoList();
        addInfoListQRPayType.setName("QRPAY_TYPE");
        addInfoListQRPayType.setValue(transDtlTransfer.getQrPayType());

        if(Objects.nonNull(transDtlTransfer.getRate())) {
            AddInfoList addInfoListRate = new AddInfoList();
            addInfoListRate.setName("TREASURY.RATE");
//            BigDecimal treasuryRate = BigDecimal.ONE.divide(
//                BigDecimal.valueOf(Double.valueOf(transDtlTransfer.getRate())), 6, RoundingMode.HALF_UP);
//            addInfoListRate.setValue(treasuryRate.toString());
            addInfoListRate.setValue(transDtlTransfer.getExchangeRate().toString());
            listAddInfo.add(addInfoListRate);
        }

        AddInfoList addInfoListCampaignCode = new AddInfoList();
        addInfoListCampaignCode.setName("MBC.CAMPAIGN.CODE");
        addInfoListCampaignCode.setValue(transDtlTransfer.getCampaignCode());
        listAddInfo.add(addInfoListCampaignCode);

        AddInfoList addInfoListCCY = new AddInfoList();
        addInfoListCCY.setName("AMOUNT_TYPE");
        addInfoListCCY.setValue(transDtlTransfer.getAmountType());

        
        listAddInfo.add(addInfoList);
        listAddInfo.add(addInfoListService);
        listAddInfo.add(addInfoListChannel);
        listAddInfo.add(addInfoListOrgData);
        listAddInfo.add(addInfoListQRPayment);
        listAddInfo.add(addInfoListQRContent);        
        listAddInfo.add(addInfoListCCY);
        listAddInfo.add(addInfoListQRPayType);
        transferInfo.setAddInfoList(listAddInfo);

        List<String> orderingCust = new ArrayList<String>();
        orderingCust.add(transDtlTransfer.getDebitAcctName());
        PaymentDetail detailInfo = new PaymentDetail();
        detailInfo.setOrderingCust(orderingCust);
        detailInfo.setOrderingBank("MB BANK (CAMBODIA) PLC");
        List<String> sendingAddrs = new ArrayList<String>();
        sendingAddrs.add("MSCBKHPPXXX - MB BANK (CAMBODIA) PLC");
        detailInfo.setSendingAddrs(sendingAddrs);
        transferInfo.setDetailInfo(detailInfo);

        transferInfo.setAmount(amount);
        transferInfo.setBranchCode(transDtlTransfer.getBranchCode());
        transferInfo.setChargeInfo(chargeInfo);
        transferInfo.setCreditAccount(creditAccount);
        transferInfo.setCreditBank(creditBank);
        transferInfo.setDebitAccount(debitAccount);
        transferInfo.setBenAccount(benAccount);
        transferInfo.setRemark(infoTrans.getDescription());
        transferInfo.setTransferType(transDtlTransfer.getTransferType());
        transferInfo.setTransactionType(transDtlTransfer.getTransactionType());
        transferInfo.setTransactionDate(DateFormatUtils.format(transactionDate, DateUtil.DATE_TIME_SIMPLE_REVERSE_2));
        
        transferInfo.setCiftpChannel(transDtlTransfer.getCiftpChannel());
//        transferInfo.setPaymentTypeCode(transDtlTransfer.getPaymentTypeCode());

        transDtlTransfer.setTransactionDate(transactionDate);
        transferInfo.setChannel(ChannelEnum.SDK_RETAIL.getCode());

        ExecuteT24Output<DataOutput> esbOutput =
            apiFundsTransfer.makeTransfer(transferInfo, custId, requestId, infoTrans.getId());
     // ====== hard code error start =======
//        esbOutput.setSoaErrorCode("002");
//        esbOutput.setSoaErrorDesc("Request Timeout");
//        ErrorInfo errorInfo = new ErrorInfo();
//        errorInfo.setErrorCode("002");
//        errorInfo.setErrorDesc("Request Timeout");
//        esbOutput.setErrorInfo(errorInfo);
//        esbOutput.setStatus("400");
        
        if (esbOutput != null) {
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(esbOutput.getStatus())) {
                infoTrans.setFt(esbOutput.getData().getT24Ft());
                infoTrans.setStatus(Constant.COM_STATUS_COM);
                comTransRepo.saveAndFlush(infoTrans);

                transDtlTransfer.setRequestId(requestId);
                transDtlTransfer.setFt(esbOutput.getData().getT24Ft());
                transDtlTransfer.setTransHash(esbOutput.getData().getTransHash());
                transDtlTransfer.setStatus(Constant.COM_STATUS_COM);
                comTransDtlTransferRepo.saveAndFlush(transDtlTransfer);

                ComTransProcess comTransProcess = new ComTransProcess();
                comTransProcess.setFt(esbOutput.getData().getT24Ft());
                comTransProcess.setSrvcCd(infoTrans.getSrvcCd());
                comTransProcess.setTransId(infoTrans.getId());
                comTransProcess.setStatus(Constant.COM_STATUS_COM);
                comTransProcess.setErrorCode(esbOutput.getErrorInfo().getErrorCode());
                comTransProcess.setErrorDesc(esbOutput.getErrorInfo().getErrorDesc());
                comTransProcessRepo.saveAndFlush(comTransProcess);
            }else if ("002".equals(esbOutput.getSoaErrorCode())) {
                
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
        }
        else {
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
