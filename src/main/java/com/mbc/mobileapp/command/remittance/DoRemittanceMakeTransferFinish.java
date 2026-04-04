package com.mbc.mobileapp.command.remittance;

import com.mbc.common.api.CallPushMessages;
import com.mbc.common.api.models.pushnotify.*;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.command.pushnotifypartner.DoPushNotifyPartner;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.constant.TransactionAuthMethod;
import com.mbc.common.entity.*;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComMobileDeviceRepo;
import com.mbc.common.repository.ComTransDtlInternationalRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.services.CarryService;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallRemittanceService;
import com.mbc.mobileapp.api.model.remittance.input.RemittanceMakeTransferFinishInput;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceMakeTransferFinishOutput;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.remittance.finish.MakeTransferFinishRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DoRemittanceMakeTransferFinish implements Command {

    private final CallRemittanceService apiRemittance;

    private final ComTransRepo comTransRepo;

    private final ComTransProcessRepo comTransProcessRepo;

    private final ComTransDtlInternationalRepo comTransDtlInternationalRepo;

    private final CallPushMessages callPushMessages;

    private final ComMobileDeviceRepo comMobileDeviceRepo;

    private final DoCheckCampaignRemittance doCheckCampaignRemittance;

    private final DoPushNotifyPartner doPushNotifyPartner;

    @Value("${msg.type.international.transfer}")
    private String pushNotiMessageType;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        CustInfo custInfo = context.getCustomer();
        String custId = context.getCustomer().getId();
        String requestId = context.getRequest().getRequestId();
        String srvcCd = request.getSrvcCd();
        MakeTransferFinishRequest makeTransferFinishRequest = request.getMakeTransferFinishRequest();
        String transId = makeTransferFinishRequest.getTransId();
        TokenOtp otp = (TokenOtp) context.getVar(Constant.KeyVar.OTP);

        try {
            ArrayList<Command> doNext = new ArrayList<>();
            //Lấy thông tin transaction
            ComTrans infoTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(custId, srvcCd, Constant.COM_STATUS_INT, transId, request.getSessionId());
            if (Objects.isNull(infoTrans)) {
                result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                        MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
                //update trans by not found transaction
                setStatusTrans(transId);
                context.setResult(result);
                return !result.isOk();
            }

            //get detail transaction
            ComTransDtlInternational comTransDtlInternational = comTransDtlInternationalRepo.findByIdAndStatus(infoTrans.getId(), Constant.COM_STATUS_INT);
            infoTrans.setAuthMethod(Objects.nonNull(otp.getAuthMethod()) ? otp.getAuthMethod() : TransactionAuthMethod.AUTH_METHOD_SMS);
            if (Objects.isNull(comTransDtlInternational)) {
                result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                        MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
                //update trans by not found transaction
                setStatusTrans(transId);
                context.setResult(result);
                return !result.isOk();
            }
            comTransDtlInternational.setRequestId(requestId);

            RemittanceMakeTransferFinishInput input = new RemittanceMakeTransferFinishInput();
            input.setRequestMessageID(comTransDtlInternational.getRequestMessageId());
            input.setPartnerCode(CommonServiceConstant.Channel.MBC_MOBILE.name());
            input.setFeePartnerCurrency(comTransDtlInternational.getChargeCurrency());
            input.setFeePartner(comTransDtlInternational.getChargeAmount().toString());

            comTransDtlInternational.setTransactionDate(new Date());
            ExecuteT24Output<RemittanceMakeTransferFinishOutput> output = apiRemittance.finish(input, transId, custId, requestId);

            //set timeout
//            output.setStatus("400");
//            output.setSoaErrorCode("002");
//            output.setSoaErrorDesc("Request Timeout");

            if (Objects.isNull(output)) {
                AppLog.error(" DoRemittanceMakeTransferFinish timeout ");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
                updateTrans(output, infoTrans, comTransDtlInternational);
                context.setResult(result);

                //CHECK CAMPAIGN CASHBACK                      
                CarryService carryService = new CarryService(doCheckCampaignRemittance);
                carryService.execute(context);

                return !result.isOk();
            }
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                RemittanceMakeTransferFinishOutput makeTransferFinishOutput = output.getData();
                response.setMakeTransferFinishOutput(makeTransferFinishOutput);
                updateTrans(output, infoTrans, comTransDtlInternational);
                context.setResult(result);
                context.setResponse(response);
                context.putVar(Constant.KeyVar.CURRENCY_TRANSACTION_EXECUTE, infoTrans);
                context.putVar("ComTransDtlInternational", comTransDtlInternational);

                //CHECK CAMPAIGN CASHBACK
                CarryService carryService = new CarryService(doCheckCampaignRemittance);
                carryService.execute(context);

                return !result.isOk();
            } else {
                String errorDesc = output.getErrorInfo().getErrorDesc();

                if ("4948".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.EXPIRED_DATE.getDesc(), false, MBCResponseCode.EXPIRED_DATE.getCode());
                } else if ("4925".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.PAYMENT_ERROR.getDesc(), false, MBCResponseCode.PAYMENT_ERROR.getCode());
                } else if ("4947".equals(output.getSoaErrorCode())) {

                    String amount = comTransDtlInternational.getDebitAmount().add(comTransDtlInternational.getChargeAmount()).toString();
                    String time = DateFormatUtils.format(comTransDtlInternational.getTransactionDate(), DateUtil.FULL_TIME);

                    StringBuilder content = new StringBuilder("You currently have 01 Pending Transaction $");
                    content.append(amount);
                    content.append(" at ");
                    content.append(time);
                    content.append(". We will notice to you the transaction result after 3 hours during bank working time. For further support, please contact bank hotline 023 968 686. Thank you.");

                    pushNotify(content.toString(), custInfo, requestId);
                    //do doCheckCampaignRemittance first then do doPushNotifyPartner after
                    this.preparePartnerNotify(context, comTransDtlInternational);
                    doNext.add(doPushNotifyPartner);

                    result = new SimpleResult(MBCResponseCode.AML_PENDING.getDesc(), false, MBCResponseCode.AML_PENDING.getCode());
                } else if ("4926".equals(output.getSoaErrorCode()) || "002".equals(output.getSoaErrorCode()) || "4939".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.PAYMENT_TIMEOUT.getDesc(), false, MBCResponseCode.PAYMENT_TIMEOUT.getCode());
                } else if ("4951".equals(output.getSoaErrorCode())) {

                    String amount = comTransDtlInternational.getDebitAmount().add(comTransDtlInternational.getChargeAmount()).toString();
                    String time = DateFormatUtils.format(comTransDtlInternational.getTransactionDate(), DateUtil.FULL_TIME);

                    StringBuilder content = new StringBuilder("You currently have 01 Pending Transaction $");
                    content.append(amount);
                    content.append(" at ");
                    content.append(time);
                    content.append(". We will notice to you the transaction result after 3 hours during bank working time. For further support, please contact bank hotline 023 968 686. Thank you.");

                    pushNotify(content.toString(), custInfo, requestId);

                    result = new SimpleResult(MBCResponseCode.AML_PENDING_PCRT_AUTO.getDesc(), false, MBCResponseCode.AML_PENDING_PCRT_AUTO.getCode());
                } else if ("4945".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.REMITTANCE_LIMIT_DAY.getDesc(), false, MBCResponseCode.REMITTANCE_LIMIT_DAY.getCode());
                } else if ("4946".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.REMITTANCE_LIMIT_YEAR.getDesc(), false, MBCResponseCode.REMITTANCE_LIMIT_YEAR.getCode());
                } else if ("4983".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.REMITTANCE_LIMIT_YEAR.getDesc(), false, MBCResponseCode.REMITTANCE_LIMIT_YEAR.getCode());
                } else if ("4984".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult("The campaign period has ended. Please perform the transaction again with the regular fee.",
                            false, output.getSoaErrorCode());
                } else if ("4985".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult("All discount fee transactions have been used. Please perform the transaction again with the regular fee.",
                            false, output.getSoaErrorCode());
                } else if ("4986".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult("You have used all your discount fee transactions. Please perform the transaction again with the regular fee.",
                            false, output.getSoaErrorCode());
                } else {
                    if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                        errorDesc = output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                    }
                    result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());
                }
                updateTrans(output, infoTrans, comTransDtlInternational);
                context.putVar("ComTransDtlInternational", comTransDtlInternational);
                context.setResult(result);

                //CHECK CAMPAIGN CASHBACK
                //do doCheckCampaignRemittance first then do doPushNotifyPartner after
                doNext.add(0, doCheckCampaignRemittance);
                CarryService carryService = new CarryService(doNext);
                carryService.execute(context);
            }
            return !result.isOk();

        } catch (Exception e) {
            AppLog.error("ERROR DoRemittanceMakeTransferFinish: ", e);
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

        ComTransDtlInternational comTransDtlInternational = comTransDtlInternationalRepo.findById(transId).orElseThrow();
        comTransDtlInternational.setStatus(Constant.COM_STATUS_FAIL);
        comTransDtlInternationalRepo.saveAndFlush(comTransDtlInternational);

        ComTransProcess comTransProcess = new ComTransProcess();
        comTransProcess.setSrvcCd(comTrans.getSrvcCd());
        comTransProcess.setTransId(comTrans.getId());
        comTransProcess.setStatus(Constant.COM_STATUS_FAIL);
        comTransProcessRepo.saveAndFlush(comTransProcess);
    }

    private void updateTrans(ExecuteT24Output<RemittanceMakeTransferFinishOutput> executeT24Output, ComTrans transInfo, ComTransDtlInternational comTransDtlInternational) {
        if (Objects.nonNull(executeT24Output)) {
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(executeT24Output.getStatus())) {
                transInfo.setFt(executeT24Output.getData().getTransactionID());
                transInfo.setStatus(Constant.COM_STATUS_COM);
                transInfo = comTransRepo.saveAndFlush(transInfo);

                comTransDtlInternational.setFt(transInfo.getFt());
                comTransDtlInternational.setStatus(Constant.COM_STATUS_COM);
                comTransDtlInternationalRepo.saveAndFlush(comTransDtlInternational);

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

                if ("4947".equals(executeT24Output.getSoaErrorCode()) || "4951".equals(executeT24Output.getSoaErrorCode())) {
                    transInfo.setStatus(CommonServiceConstant.Referral.PROCESSING);
                    comTransDtlInternational.setStatus(CommonServiceConstant.Referral.PROCESSING);
                    comTransProcess.setStatus(CommonServiceConstant.Referral.PROCESSING);
                } else if ("4926".equals(executeT24Output.getSoaErrorCode()) || "002".equals(executeT24Output.getSoaErrorCode()) || "4939".equals(executeT24Output.getSoaErrorCode())) {
                    transInfo.setStatus(Constant.COM_STATUS_PND);
                    comTransDtlInternational.setStatus(Constant.COM_STATUS_PND);
                    comTransProcess.setStatus(Constant.COM_STATUS_PND);
                } else {
                    transInfo.setStatus(Constant.COM_STATUS_FAIL);
                    comTransDtlInternational.setStatus(Constant.COM_STATUS_FAIL);
                    comTransProcess.setStatus(Constant.COM_STATUS_FAIL);
                }
                comTransRepo.saveAndFlush(transInfo);
                comTransDtlInternational.setTransactionDate(new Date());
                comTransDtlInternationalRepo.saveAndFlush(comTransDtlInternational);

                comTransProcess.setSrvcCd(transInfo.getSrvcCd());
                comTransProcess.setTransId(transInfo.getId());
                comTransProcess.setErrorCode(executeT24Output.getErrorInfo().getErrorCode());
                comTransProcess.setErrorDesc(executeT24Output.getErrorInfo().getErrorDesc());
                comTransProcessRepo.saveAndFlush(comTransProcess);
            }
        } else {
            transInfo.setStatus(Constant.COM_STATUS_PND);
            comTransRepo.saveAndFlush(transInfo);

            comTransDtlInternational.setTransactionDate(new Date());
            comTransDtlInternational.setStatus(Constant.COM_STATUS_PND);
            comTransDtlInternationalRepo.saveAndFlush(comTransDtlInternational);

            ComTransProcess comTransProcess = new ComTransProcess();
            comTransProcess.setSrvcCd(transInfo.getSrvcCd());
            comTransProcess.setTransId(transInfo.getId());
            comTransProcess.setStatus(Constant.COM_STATUS_PND);
            comTransProcessRepo.saveAndFlush(comTransProcess);

        }
    }

    private void pushNotify(String contentNotify, CustInfo custInfo, String requestId) {
        try {
            List<ComMobileDevice> tokens = comMobileDeviceRepo.findByCustIdAndStatus(custInfo.getId(), Constant.STATUS_1);

            List<MessagesNotify> lstMessageNoti = new ArrayList<>();
            String msgId = Utility.getUUID();
//            Cust custInfo = custRepoExtend.findById(custId).orElseThrow();
            List<Receiver> listReceiverNoti = new ArrayList<>();
            for (ComMobileDevice device : tokens) {
                Receiver receiverNotify = new Receiver();
                receiverNotify.setReceiverId(device.getDeviceToken());
                receiverNotify.setReceiverType("FCM_TOKEN");
                receiverNotify.setReceiverAppId("MOBILEAPP_CAM");
                receiverNotify.setReceiverUserId(Objects.nonNull(custInfo.getUserId()) ? custInfo.getUserId() : custInfo.getHostCifId());
                listReceiverNoti.add(receiverNotify);
            }

            BodyDataExtendParam dataParam = new BodyDataExtendParam();
            dataParam.setDataKey("content-type");
            dataParam.setDataValue("0");
            List<BodyDataExtendParam> lstDataParam = new ArrayList<>();
            lstDataParam.add(dataParam);

            MessagesNotify messagesNotify = new MessagesNotify();
            messagesNotify.setBodyTitle("Remittance to Vietnam Transaction");
            messagesNotify.setBodyContent(contentNotify);
            messagesNotify.setBodyShortContent(contentNotify);
            messagesNotify.setListReceiver(listReceiverNoti);
            messagesNotify.setBodyListDataExtendParam(lstDataParam);
            messagesNotify.setMessageType(pushNotiMessageType);
            messagesNotify.setMessageId(msgId);
            lstMessageNoti.add(messagesNotify);

            MessagesInput smsMessagesInput = new MessagesInput();
            smsMessagesInput.setListMessages(lstMessageNoti);

            ExecuteT24Output<NotifyOutput> output = callPushMessages
                    .pushNotifyAndSmsLuckeyMoney(smsMessagesInput, custInfo.getId(), requestId);
            if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                AppLog.info("International Transfer HIT pushNoti fail: " + output.getSoaErrorCode() + " - "
                        + output.getSoaErrorDesc());
            }
        } catch (Exception e) {
            AppLog.error("International Transfer HIT pushNoti exception : " + e.getMessage());
        }

    }

    private void preparePartnerNotify(ProcessContext context, ComTransDtlInternational comTransDtlInternational) {
        CustInfo custInfo = context.getCustomer();
        ComPartnerNotify partnerNotify = ComPartnerNotify.builder()
                .serviceName(comTransDtlInternational.getTransferType())
                .custId(custInfo.getHostCifId())
                .idType(custInfo.getIdTypType())
                .idCard(custInfo.getIdTypNo())
                .bankRefId(comTransDtlInternational.getFt())
                .transactionDate(comTransDtlInternational.getCreatedDt())
                .amount(comTransDtlInternational.getDebitAmount())
                .currency(comTransDtlInternational.getDebitCurrency())
                .fee(comTransDtlInternational.getChargeAmount())
                .detail(comTransDtlInternational.getPurpose())
                .mbcTransaction(JSON.stringify(comTransDtlInternational))
                .build();
        context.putVar("PARTNER_NOTIFY", partnerNotify);
    }
}
