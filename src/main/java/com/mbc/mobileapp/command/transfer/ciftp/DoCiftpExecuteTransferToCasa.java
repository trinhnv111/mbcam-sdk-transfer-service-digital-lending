package com.mbc.mobileapp.command.transfer.ciftp;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DoCiftpExecuteTransferToCasa implements Command {
    @Autowired
    private ComTransRepo comTransRepo;

    @Autowired
    private ComTransProcessRepo comTransProcessRepo;

    @Autowired
    private ComTransDtlTransferRepo comTransDtlTransferRepo;

    @Autowired
    private ApiFundsTransfer apiFundsTransfer;

    @Autowired
    private ComTransDomesticChargeRepo comTransDomesticChargeRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CustInfo customer = context.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String srvcCd = request.getSrvcCd();
        String custId = customer.getId();
        String requestId = request.getRequestId();
        String transId = request.getTransId();        
        TokenOtp otp = (TokenOtp) context.getVar(Constant.KeyVar.OTP);
        
        try {
            //lấy thông tin transaction
            ComTrans infoTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(custId, srvcCd,
                    Constant.COM_STATUS_INT, transId, request.getSessionId());
            String responseStatus = "";

            if (Objects.nonNull(infoTrans)) {
                //lấy detail trans
                ComTransDtlTransfer comTransDtlTransfer = comTransDtlTransferRepo.findByIdAndStatus(infoTrans.getId(), Constant.COM_STATUS_INT);
                if (Objects.nonNull(comTransDtlTransfer)) {
                    
                    if(!request.getCiftpSettlement().equals(comTransDtlTransfer.getCiftpChannel())) {
                        result = new SimpleResult(ResponseCode.TRANSACTION_ID_INCORRECT.getDesc(), false,
                            ResponseCode.TRANSACTION_ID_INCORRECT.getCode());
                    }else {
                        ExecuteT24Output<DataOutput> esbOutput = null;
                        infoTrans.setAuthMethod(Objects.nonNull(otp.getAuthMethod()) ? otp.getAuthMethod() : TransactionAuthMethod.AUTH_METHOD_SMS);
                        esbOutput = executeTransfer(infoTrans, comTransDtlTransfer, custId, requestId);                                            
                        
                        if (esbOutput != null) {
                            responseStatus = esbOutput.getStatus();
                            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(responseStatus)) {
                                response.setFt(esbOutput.getData().getT24Ft());
                                response.setTransId(transId);
                                response.setTransHash(esbOutput.getData().getTransHash());
                                context.setResponse(response);
                                context.putVar(Constant.KeyVar.CURRENCY_TRANSACTION_EXECUTE, infoTrans);

                            }else if("002".equals(esbOutput.getSoaErrorCode())) {
                                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                                    ResponseCode.REQUEST_TIMEOUT.getCode());
                            } else {
                                String errorDesc = esbOutput.getErrorInfo().getErrorDesc();
                                if (!Utility.isNull(esbOutput.getErrorInfo().getErrorDetail())) {
                                    errorDesc =
                                            esbOutput.getErrorInfo().getErrorDesc() + " - " + esbOutput.getErrorInfo().getErrorDetail();
                                }
                                result = new SimpleResult(errorDesc, false, esbOutput.getErrorInfo().getErrorCode());
                            }
                        } else {
                            result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                                    ResponseCode.REQUEST_TIMEOUT.getCode());
                        }
                    }             
                    
                } else {
                    result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                            MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
                }
            } else {
                result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                        MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
            }


        } catch (Exception e) {
            log.error("[Exception execute to casa] requestId: {} desc: {}", request.getRequestId(), e);
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
        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setAccountName(transDtlTransfer.getCreditAcctName());
        creditAccount.setAccountNumber(transDtlTransfer.getCreditAcctNo());
        creditAccount.setAccountCurrency(transDtlTransfer.getCreditCurrency());
        creditAccount.setAccountType(CommonServiceConstant.AccountType.ACCOUNT.name());

        DebitAccount debitAccount = new DebitAccount();
        debitAccount.setAccountNumber(transDtlTransfer.getDebitAcctNo());
        debitAccount.setAccountName(transDtlTransfer.getDebitAcctName());
        debitAccount.setAccountCurrency(transDtlTransfer.getDebitCurrency());
        debitAccount.setAccountType(CommonServiceConstant.AccountType.ACCOUNT.name());

        Amount amount = new Amount();
        amount.setAmount(transDtlTransfer.getDebitAmount());
        amount.setCurrency(transDtlTransfer.getDebitCurrency());
        amount.setBenAmount(transDtlTransfer.getCreditAmount());
        amount.setBenCurrency(transDtlTransfer.getCreditCurrency());
        
        BenAccount benAccount = new BenAccount();
        benAccount.setAccountName(creditAccount.getAccountName());                            
        benAccount.setAccountNumber(creditAccount.getAccountNumber());
        benAccount.setAccountType(CommonServiceConstant.AccountType.ACCOUNT.name());
        benAccount.setAccountCurrency(creditAccount.getAccountCurrency());
        benAccount.setBenAmount(String.valueOf(transDtlTransfer.getCreditAmount()));

        CreditBank creditBank = new CreditBank();
        creditBank.setCode(transDtlTransfer.getDestBankPartiCode());
        creditBank.setName(transDtlTransfer.getDestBankName());

        ChargeInfo chargeInfo = new ChargeInfo();
        ChargeAccount chargeAccount = new ChargeAccount();
        chargeAccount.setAccountCurrency(transDtlTransfer.getDebitCurrency());
        chargeAccount.setAccountName(transDtlTransfer.getDebitAcctName());
        chargeAccount.setAccountNumber(transDtlTransfer.getDebitAcctNo());
        chargeAccount.setAccountType(CommonServiceConstant.AccountType.ACCOUNT.name());
        chargeInfo.setChargeAccount(chargeAccount);

        List<AddInfoList> listAddInfo = new ArrayList<AddInfoList>();

        AddInfoList addInfoList0 = new AddInfoList();
        addInfoList0.setName("SERVICE");
        addInfoList0.setValue(CommonServiceConstant.Service.CASA_TO_CASA.name());
        listAddInfo.add(addInfoList0);

        AddInfoList addInfoList1 = new AddInfoList();
        addInfoList1.setName("CHANNEL");
        addInfoList1.setValue(ChannelEnum.SDK_RETAIL.getCode());
        listAddInfo.add(addInfoList1);

        AddInfoList addInfoList2 = new AddInfoList();
        addInfoList2.setName("ORG.DATA");
        addInfoList2.setValue("SENDER");
        listAddInfo.add(addInfoList2);

        AddInfoList addInfoList3 = new AddInfoList();
        addInfoList3.setName("QRPAYMENT");
        addInfoList3.setValue(!Utility.isNull(transDtlTransfer.getPayloadQr()) ? Constant.YES_1 : Constant.NO_1);
        listAddInfo.add(addInfoList3);

        AddInfoList addInfoListQRContent = new AddInfoList();
        addInfoListQRContent.setName("QRCONTENT");
        addInfoListQRContent.setValue(transDtlTransfer.getPayloadQr());
        listAddInfo.add(addInfoListQRContent);

        if(Objects.nonNull(transDtlTransfer.getRate())) {
            AddInfoList addInfoList5 = new AddInfoList();
            addInfoList5.setName("TREASURY.RATE");
//            BigDecimal treasuryRate = BigDecimal.ONE.divide(
//                BigDecimal.valueOf(Double.valueOf(transDtlTransfer.getRate())), 6, RoundingMode.HALF_UP);
//            addInfoList5.setValue(treasuryRate.toString());
            addInfoList5.setValue(transDtlTransfer.getExchangeRate().toString());
            listAddInfo.add(addInfoList5);
        }

        AddInfoList addInfoListCampaignCode = new AddInfoList();
        addInfoListCampaignCode.setName("MBC.CAMPAIGN.CODE");
        addInfoListCampaignCode.setValue(transDtlTransfer.getCampaignCode());
        listAddInfo.add(addInfoListCampaignCode);

        AddInfoList addInfoList6 = new AddInfoList();
        addInfoList6.setName("AMOUNT_TYPE");
        addInfoList6.setValue(transDtlTransfer.getAmountType());
        listAddInfo.add(addInfoList6);

        List<ComTransDomesticCharge> domesticCharge = comTransDomesticChargeRepo.findByTransId(transDtlTransfer.getId());
        List<ChargeList> cList = new ArrayList<>();
        for (ComTransDomesticCharge charge : domesticCharge) {
            ChargeList chargeList = new ChargeList();
            chargeList.setAmount(transDtlTransfer.getDebitChargeAmount());
            chargeList.setCode(charge.getChargeCode());
            chargeList.setCurrency(transDtlTransfer.getDebitCurrency());
            cList.add(chargeList);
        }
        chargeInfo.setChargeList(cList);
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
        transferInfo.setPaymentTypeCode(transDtlTransfer.getPaymentTypeCode());
        transferInfo.setChannel(ChannelEnum.SDK_RETAIL.getCode());

        transDtlTransfer.setTransactionDate(transactionDate);
        ExecuteT24Output<DataOutput> esbOutput = apiFundsTransfer.makeTransfer(transferInfo, custId, requestId, infoTrans.getId());
        if (esbOutput != null) {
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(esbOutput.getStatus())) {
                infoTrans.setFt(esbOutput.getData().getT24Ft());
                infoTrans.setStatus(Constant.COM_STATUS_COM);
                infoTrans = comTransRepo.saveAndFlush(infoTrans);

                transDtlTransfer.setRequestId(requestId);
                transDtlTransfer.setFt(esbOutput.getData().getT24Ft());
                transDtlTransfer.setStatus(Constant.COM_STATUS_COM);
                transDtlTransfer.setTransHash(esbOutput.getData().getTransHash());
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
            } else {

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
        } else {
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
