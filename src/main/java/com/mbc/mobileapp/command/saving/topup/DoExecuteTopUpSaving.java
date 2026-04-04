package com.mbc.mobileapp.command.saving.topup;

import com.mbc.common.api.CallPushMessages;
import com.mbc.common.api.models.pushnotify.*;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.constant.TransactionAuthMethod;
import com.mbc.common.entity.*;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComMobileDeviceRepo;
import com.mbc.common.repository.ComTransDtlSavingRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallSavingService;
import com.mbc.mobileapp.api.model.saving.topup.TopUpSavingDepositOutput;
import com.mbc.mobileapp.api.model.saving.topup.TopupSavingDepositInput;
import com.mbc.mobileapp.config.SavingFlexiDepositConfig;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.constant.SavingDepositConstant;
import com.mbc.mobileapp.constant.format.FormatNumber;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoExecuteTopUpSaving implements Command {

    private final CallSavingService callSavingService;

    private final ComTransRepo comTransRepo;

    private final ComTransDtlSavingRepo comTransDtlSavingRepo;

    private final ComTransProcessRepo comTransProcessRepo;

    private final CallPushMessages callPushMessages;

    private final ComMobileDeviceRepo comMobileDeviceRepo;


    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        String transId = request.getTransId();
        String requestId = request.getRequestId();
        String srvcCd = request.getSrvcCd();
        String custId = customer.getId();
        TokenOtp otp = (TokenOtp) context.getVar(Constant.KeyVar.OTP);

        try {

            ComTrans comTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(custId, srvcCd,
                    Constant.COM_STATUS_INT, transId, request.getSessionId());
            if (Objects.isNull(comTrans)) {
                result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                        MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
                setStatusTrans(transId);
                context.setResult(result);
                return !result.isOk();
            }

            comTrans.setAuthMethod(Objects.nonNull(otp.getAuthMethod()) ? otp.getAuthMethod() : TransactionAuthMethod.AUTH_METHOD_SMS);
            //lấy detail
            ComTransDtlSaving comTransDtlSaving = comTransDtlSavingRepo.findByIdAndStatus(comTrans.getId(), Constant.COM_STATUS_INT);
            if (Objects.isNull(comTransDtlSaving)) {
                result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                        MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
                setStatusTrans(transId);
                context.setResult(result);
                return !result.isOk();
            }
            comTransDtlSaving.setRequestId(requestId);
            //call api topup
            TopupSavingDepositInput input = TopupSavingDepositInput.builder()
                    .savingId(comTransDtlSaving.getSavingAcctNo())
                    .savingName(comTransDtlSaving.getSavingAcctName())
                    .savingCurrency(comTransDtlSaving.getSavingCurrency())
                    .savingType(SavingDepositConstant.AccountType.ACCOUNT)
                    .topUpAmount(comTransDtlSaving.getSavingAmount().toString())
                    .topUpCurrency(comTransDtlSaving.getSavingCurrency())
                    .remark(comTrans.getDescription())
                    .channel(CommonServiceConstant.CHANNEL_MOBILE_RETAIL)
                    .debitAccount(
                            TopupSavingDepositInput.DebitAccountTopup.builder()
                                    .accountNumber(comTransDtlSaving.getDebitAcctNo())
                                    .accountName(comTransDtlSaving.getDebitAcctName())
                                    .accountType(SavingDepositConstant.AccountType.ACCOUNT)
                                    .accountCurrency(comTransDtlSaving.getDebitCurrency())
                                    .build()
                    )
                    .build();

            ExecuteT24Output<TopUpSavingDepositOutput> output = callSavingService.topupSavingDeposit(input, custId, requestId);
            if (Objects.isNull(output)) {
                AppLog.error(" Topup Flexi timeout ");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
                updateTrans(output, comTrans, comTransDtlSaving);
                context.setResult(result);
                return !result.isOk();
            }
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {

                TopUpSavingDepositOutput topUpSavingDepositOutput = output.getData();
                response.setTopUpSavingDepositOutput(topUpSavingDepositOutput);
                //update
                updateTrans(output, comTrans, comTransDtlSaving);
                context.setResult(result);
                context.setResponse(response);

                //get token
                List<ComMobileDevice> tokens = comMobileDeviceRepo
                        .findByCustIdAndDeviceIdAndStatus(customer.getId(), request.getDeviceIdCommon(), Constant.STATUS_1);
                String token = tokens.get(0).getDeviceToken();
                //push noti
                pushNotify(token, comTransDtlSaving, customer, comTrans);
                this.preparePartnerNotify(context, comTransDtlSaving);

            } else {
                if ("002".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());
                } else {
                    String errorDesc = output.getErrorInfo().getErrorDesc();
                    if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                        errorDesc =
                                output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                    }
                    result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());

//                    result = new SimpleResult(MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getDesc(), false,
//                            MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getErrorCode());

                }
                updateTrans(output, comTrans, comTransDtlSaving);
                context.setResult(result);
            }
            return !result.isOk();

        } catch (Exception e) {
            AppLog.error("DoTopUpFlexiDeposit ERROR", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
            setStatusTrans(transId);
            context.setResult(result);
            return !result.isOk();
        }
    }

    private void setStatusTrans(String transId) {
        ComTrans comTrans = comTransRepo.findById(transId).orElseThrow();
        comTrans.setStatus(Constant.COM_STATUS_FAIL);
        comTransRepo.saveAndFlush(comTrans);

        ComTransDtlSaving comTransDtlSaving = comTransDtlSavingRepo.findById(transId).orElseThrow();
        comTransDtlSaving.setStatus(Constant.COM_STATUS_FAIL);
        comTransDtlSavingRepo.saveAndFlush(comTransDtlSaving);

        ComTransProcess comTransProcess = new ComTransProcess();
        comTransProcess.setSrvcCd(comTrans.getSrvcCd());
        comTransProcess.setTransId(comTrans.getId());
        comTransProcess.setStatus(Constant.COM_STATUS_FAIL);
        comTransProcessRepo.saveAndFlush(comTransProcess);
    }

    private void updateTrans(ExecuteT24Output<TopUpSavingDepositOutput> executeT24Output, ComTrans transInfo, ComTransDtlSaving comTransDtlSaving) {
        if (Objects.nonNull(executeT24Output)) {
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(executeT24Output.getStatus())) {
                transInfo.setStatus(Constant.COM_STATUS_COM);
                comTransDtlSaving.setFt(executeT24Output.getData().getT24VersionId());
                transInfo.setFt(executeT24Output.getData().getT24VersionId());

                transInfo = comTransRepo.saveAndFlush(transInfo);


                comTransDtlSaving.setFt(executeT24Output.getData().getT24VersionId());
                comTransDtlSaving.setStatus(Constant.COM_STATUS_COM);
                comTransDtlSaving.setDebitAmount(new BigDecimal(executeT24Output.getData().getDebitAmount()));
                comTransDtlSavingRepo.saveAndFlush(comTransDtlSaving);

                ComTransProcess comTransProcess = new ComTransProcess();
                comTransProcess.setFt(transInfo.getFt());
                comTransProcess.setSrvcCd(transInfo.getSrvcCd());
                comTransProcess.setTransId(transInfo.getId());
                comTransProcess.setStatus(Constant.COM_STATUS_COM);
                comTransProcess.setErrorCode(executeT24Output.getErrorInfo().getErrorCode());
                comTransProcess.setErrorDesc(executeT24Output.getErrorInfo().getErrorDesc());
                comTransProcessRepo.saveAndFlush(comTransProcess);
            } else {
                ComTransProcess comTransProcess = new ComTransProcess();
                if ("002".equals(executeT24Output.getSoaErrorCode())) {
                    transInfo.setStatus(Constant.COM_STATUS_PND);
                    comTransDtlSaving.setStatus(Constant.COM_STATUS_PND);
                    comTransProcess.setStatus(Constant.COM_STATUS_PND);
                } else {
                    transInfo.setStatus(Constant.COM_STATUS_FAIL);
                    comTransDtlSaving.setStatus(Constant.COM_STATUS_FAIL);
                    comTransProcess.setStatus(Constant.COM_STATUS_FAIL);
                }

                comTransRepo.saveAndFlush(transInfo);

                comTransDtlSavingRepo.saveAndFlush(comTransDtlSaving);

                comTransProcess.setSrvcCd(transInfo.getSrvcCd());
                comTransProcess.setTransId(transInfo.getId());
                comTransProcess.setErrorCode(executeT24Output.getErrorInfo().getErrorCode());
                comTransProcess.setErrorDesc(executeT24Output.getErrorInfo().getErrorDesc());
                comTransProcessRepo.saveAndFlush(comTransProcess);
            }
        } else {
            transInfo.setStatus(Constant.COM_STATUS_PND);
            comTransRepo.saveAndFlush(transInfo);

            comTransDtlSaving.setStatus(Constant.COM_STATUS_PND);
            comTransDtlSavingRepo.saveAndFlush(comTransDtlSaving);

            ComTransProcess comTransProcess = new ComTransProcess();
            comTransProcess.setSrvcCd(transInfo.getSrvcCd());
            comTransProcess.setTransId(transInfo.getId());
            comTransProcess.setStatus(Constant.COM_STATUS_PND);
            comTransProcessRepo.saveAndFlush(comTransProcess);

        }
    }

    private void pushNotify(String token, ComTransDtlSaving comTransDtlSaving, CustInfo custInfo, ComTrans comTrans) {
        try {

            String amount = comTransDtlSaving.getSavingAmount().toString();
            if (Constant.CURRENCY_TYPE_USD.equals(comTransDtlSaving.getDebitCurrency())) {
                amount = FormatNumber.formatAmountWithCommasTwoDecimal(amount);
            } else {
                amount = Utility.formatAmountWithCommas(amount);
            }

            List<MessagesNotify> lstMessageNoti = new ArrayList<>();
            String msgId = Utility.getUUID();
            List<Receiver> listReceiverNoti = new ArrayList<>();

            Receiver receiverNotify = new Receiver();
            receiverNotify.setReceiverId(token);
            receiverNotify.setReceiverType("FCM_TOKEN");
            receiverNotify.setReceiverAppId("MOBILEAPP_CAM");
            receiverNotify.setReceiverUserId(custInfo.getUserId());
            listReceiverNoti.add(receiverNotify);

            String smsContent = "Transaction time: " + DateUtil.convertToDateString(comTransDtlSaving.getUpdatedDate(), DateUtil.DATETIME_WITH_SLASH)
                    + " "
                    + SavingDepositConstant.FLEXI_CONTENT_HEADER_NOTIFY + " deposit account: " + comTransDtlSaving.getSavingAcctNo()
                    + " Top Up amount: " + amount + " " + comTransDtlSaving.getSavingCurrency();


            BodyDataExtendParam dataParam = new BodyDataExtendParam();
            dataParam.setDataKey("content-type");
            dataParam.setDataValue("0");


            BodyDataExtendParam dataParam2 = new BodyDataExtendParam();
            dataParam2.setDataKey("body");
            dataParam2.setDataValue("Transaction time: " + DateUtil.convertToDateString(comTransDtlSaving.getUpdatedDate(), DateUtil.DATETIME_WITH_SLASH) + "<br>" +
                    SavingDepositConstant.FLEXI_CONTENT_HEADER_NOTIFY + " deposit account: " + comTransDtlSaving.getSavingAcctNo() + "<br>" +
                    "Top Up amount: " + amount + " " + comTransDtlSaving.getSavingCurrency() + "<br>" +
                    "Description: " + comTrans.getDescription() + "<br>" +
                    "For any assistance, please contact MBCambodia's hotline at (+855)23968 686. Thank you.");
            List<BodyDataExtendParam> lstDataParam = new ArrayList<>();
            lstDataParam.add(dataParam);
            lstDataParam.add(dataParam2);

            MessagesNotify messagesNotify = new MessagesNotify();
            messagesNotify.setBodyTitle("Top Up Flexi Term Deposit Successfully");
            messagesNotify.setBodyContent(smsContent);
            messagesNotify.setBodyShortContent(smsContent);
            messagesNotify.setListReceiver(listReceiverNoti);
            messagesNotify.setBodyListDataExtendParam(lstDataParam);
            messagesNotify.setMessageType(SavingFlexiDepositConfig.MSG_TYPE);
            messagesNotify.setMessageId(msgId);
            lstMessageNoti.add(messagesNotify);

            MessagesInput smsMessagesInput = new MessagesInput();
            smsMessagesInput.setListMessages(lstMessageNoti);

            ExecuteT24Output<NotifyOutput> output = callPushMessages
                    .pushNotifyAndSmsLuckeyMoney(smsMessagesInput, custInfo.getId(), Utility.getUUID());
            if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                AppLog.info("DoOpenRealTimeDeposit pushNoti fail: " + output.getSoaErrorCode() + " - "
                        + output.getSoaErrorDesc());
            }
        } catch (Exception e) {
            log.error("Do Topup pushNoti exception : ", e.fillInStackTrace());
        }

    }

    private void preparePartnerNotify(ProcessContext context, ComTransDtlSaving saving) {
        CustInfo custInfo = context.getCustomer();
        ComPartnerNotify notify = ComPartnerNotify.builder()
                .serviceName(context.getRequest().getSrvcCd())
                .bankRefId(saving.getFt())
                .amount(saving.getSavingAmount())
                .currency(saving.getSavingCurrency())
                .custId(custInfo.getHostCifId())
                .transactionDate(new Date())
                .mbcTransaction(JSON.stringify(saving))
                .build();
        context.putVar("PARTNER_NOTIFY", notify);
    }
}
