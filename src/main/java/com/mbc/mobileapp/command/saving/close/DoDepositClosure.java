package com.mbc.mobileapp.command.saving.close;

import com.mbc.common.api.CallPushMessages;
import com.mbc.common.api.models.pushnotify.*;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.*;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComMobileDeviceRepo;
import com.mbc.common.repository.ComTransDtlSavingSettlementRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallSavingService;
import com.mbc.mobileapp.api.model.saving.close.DepositClosureInput;
import com.mbc.mobileapp.api.model.saving.close.DepositClosureOutput;
import com.mbc.mobileapp.config.SavingFixedDepositConfig;
import com.mbc.mobileapp.constant.SavingProductEnum;
import com.mbc.mobileapp.constant.format.FormatNumber;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class DoDepositClosure implements Command {

    @Autowired
    private CallSavingService callSavingService;

    @Autowired
    private ComTransRepo comTransRepo;

    @Autowired
    private ComTransProcessRepo comTransProcessRepo;

    @Autowired
    private ComTransDtlSavingSettlementRepo comTransDtlSavingSettlementRepo;

    @Autowired
    private CallPushMessages callPushMessages;

    @Autowired
    private ComMobileDeviceRepo comMobileDeviceRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        String transId = request.getTransId();
        String custId = customer.getId();
        String srvcCd = request.getSrvcCd();
        try {
            //lấy thông tin transaction
            ComTrans infoTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(custId, srvcCd, Constant.COM_STATUS_INT, transId, request.getSessionId());
            if (Objects.isNull(infoTrans)) {
                result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                        MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
                //update trans by not found transaction
                setStatusTrans(transId);
                context.setResult(result);
                return !result.isOk();
            }

            //lấy detail trans
            ComTransDtlSavingSettlement comTransDtlSavingSettlement = comTransDtlSavingSettlementRepo.findByIdAndStatus(infoTrans.getId(), Constant.COM_STATUS_INT);
            if (Objects.isNull(comTransDtlSavingSettlement)) {
                result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                        MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
                //update trans by not found transaction
                setStatusTrans(transId);
                context.setResult(result);
                return !result.isOk();
            }


            //input call close saving
            DepositClosureInput input = new DepositClosureInput();
            input.setId(comTransDtlSavingSettlement.getSavingAcctNo());
            input.setRequestId(comTransDtlSavingSettlement.getRequestId());
            input.setNominatedAccount(comTransDtlSavingSettlement.getReceivingAccount());
            input.setPreClosureInd(Constant.YES);

            ExecuteT24Output<DepositClosureOutput> output = callSavingService.closeSavingFixedDeposit(input, customer.getId(), request.getRequestId());
            if (Objects.isNull(output)) {
                AppLog.error(" DoDepositClosure timeout ");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
                context.setResult(result);
                updateTrans(null, output, infoTrans, comTransDtlSavingSettlement);
                return !result.isOk();
            }
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                DepositClosureOutput depositClosureOutput = output.getData();
                BigDecimal receivingAmount = null;

                if (SavingProductEnum.MONTHLY.getName().equals(comTransDtlSavingSettlement.getInterestOption())) {
                    receivingAmount = depositClosureOutput.getTotAmtDue();
                } else {
                    if (Objects.nonNull(depositClosureOutput.getApproveAmount())) {
                        BigDecimal intAmtPreClose = FormatNumber.convertToAmount1(depositClosureOutput.getIntAmtPreClose());
                        BigDecimal taxAmount = FormatNumber.convertToAmount1(depositClosureOutput.getTaxAmount());
                        //USD : 2 -- KHR : 0
                        int scaleNum = Constant.CURRENCY_TYPE_USD.equals(comTransDtlSavingSettlement.getCurrency()) ? 2 : 0;
                        receivingAmount = depositClosureOutput.getApproveAmount()
                                .add(intAmtPreClose)
                                .subtract(taxAmount)
                                .setScale(scaleNum, RoundingMode.HALF_UP);
                    }
                }

                depositClosureOutput.setReceivingAmount(Objects.nonNull(receivingAmount) ? receivingAmount : depositClosureOutput.getApproveAmount());
                depositClosureOutput.setSavingName(comTransDtlSavingSettlement.getSavingAcctName());
                depositClosureOutput.setCurrency(comTransDtlSavingSettlement.getCurrency());
                depositClosureOutput.setCategoryInterest(StringUtils.isNotEmpty(comTransDtlSavingSettlement.getInterestOption()) ? SavingProductEnum.valueOfName(comTransDtlSavingSettlement.getInterestOption()).getCategory() : null);

                //update  status
                updateTrans(depositClosureOutput, output, infoTrans, comTransDtlSavingSettlement);
                response.setDepositClosureOutput(depositClosureOutput);
                context.setResponse(response);
                //push message
                List<ComMobileDevice> tokens = comMobileDeviceRepo.findByCustIdAndDeviceIdAndStatus(customer.getId(), request.getDeviceIdCommon(), Constant.STATUS_1);
                String token = tokens.get(0).getDeviceToken();

                this.pushNotify(depositClosureOutput, token, comTransDtlSavingSettlement, customer);
                this.preparePartnerNotify(context, comTransDtlSavingSettlement);

            } else {
                if ("4970".equals(output.getSoaErrorCode()) || "203".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.CLOSE_FIXED_DEPOSIT_FAIL.getDesc(), false,
                            MBCResponseCode.CLOSE_FIXED_DEPOSIT_FAIL.getCode());

                } else if ("002".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());

                } else if ("4972".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.ACCOUNT_CLOSURE_UNAVAILABLE.getDesc(), false,
                            MBCResponseCode.ACCOUNT_CLOSURE_UNAVAILABLE.getCode());
                } else if ("4971".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.CLOSED_DEPOSIT.getDesc(), false,
                            MBCResponseCode.CLOSED_DEPOSIT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                } else {
                    result = new SimpleResult(
                            output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail(),
                            false, output.getErrorInfo().getErrorCode());
                }
                updateTrans(null, output, infoTrans, comTransDtlSavingSettlement);
            }
            context.setResult(result);
            return !result.isOk();

        } catch (Exception e) {
            AppLog.error("ERROR", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
            setStatusTrans(transId);
        }

        context.setResult(result);
        return !result.isOk();
    }

    private void updateTrans(DepositClosureOutput depositClosureOutput, ExecuteT24Output<DepositClosureOutput> executeT24Output, ComTrans transInfo, ComTransDtlSavingSettlement comTransDtlSavingSettlement) {
        if (Objects.nonNull(executeT24Output)) {
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(executeT24Output.getStatus())) {
                transInfo.setStatus(Constant.COM_STATUS_COM);
                transInfo = comTransRepo.saveAndFlush(transInfo);

                comTransDtlSavingSettlement.setReceivingAmount(depositClosureOutput.getReceivingAmount());
                comTransDtlSavingSettlement.setCloseDate(executeT24Output.getData().getIntPostDate());
                comTransDtlSavingSettlement.setTaxAmount(depositClosureOutput.getTaxAmount());
                comTransDtlSavingSettlement.setInterestAmount(depositClosureOutput.getIntAmtPreClose());
                comTransDtlSavingSettlement.setStatus(Constant.COM_STATUS_COM);
                comTransDtlSavingSettlement.setSavingAmount(depositClosureOutput.getApproveAmount());
                comTransDtlSavingSettlement.setInterestRate(depositClosureOutput.getIntRatePreClose());
                comTransDtlSavingSettlementRepo.saveAndFlush(comTransDtlSavingSettlement);

                ComTransProcess comTransProcess = new ComTransProcess();
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
                    comTransDtlSavingSettlement.setStatus(Constant.COM_STATUS_PND);
                    comTransProcess.setStatus(Constant.COM_STATUS_PND);
                } else {
                    transInfo.setStatus(Constant.COM_STATUS_FAIL);
                    comTransDtlSavingSettlement.setStatus(Constant.COM_STATUS_FAIL);
                    comTransProcess.setStatus(Constant.COM_STATUS_FAIL);
                }
                comTransRepo.saveAndFlush(transInfo);
                comTransDtlSavingSettlementRepo.saveAndFlush(comTransDtlSavingSettlement);

                comTransProcess.setSrvcCd(transInfo.getSrvcCd());
                comTransProcess.setTransId(transInfo.getId());
                comTransProcess.setErrorCode(executeT24Output.getErrorInfo().getErrorCode());
                comTransProcess.setErrorDesc(executeT24Output.getErrorInfo().getErrorDesc());
                comTransProcessRepo.saveAndFlush(comTransProcess);
            }
        } else {
            transInfo.setStatus(Constant.COM_STATUS_PND);
            transInfo = comTransRepo.saveAndFlush(transInfo);
            comTransDtlSavingSettlement.setStatus(Constant.COM_STATUS_PND);
            comTransDtlSavingSettlementRepo.saveAndFlush(comTransDtlSavingSettlement);

            ComTransProcess comTransProcess = new ComTransProcess();
            comTransProcess.setSrvcCd(transInfo.getSrvcCd());
            comTransProcess.setTransId(transInfo.getId());
            comTransProcess.setStatus(Constant.COM_STATUS_PND);
            comTransProcessRepo.saveAndFlush(comTransProcess);

        }
    }

    private void setStatusTrans(String transId) {
        ComTrans comTrans = comTransRepo.findById(transId).orElseThrow();
        comTrans.setStatus(Constant.COM_STATUS_FAIL);
        comTransRepo.saveAndFlush(comTrans);

        ComTransDtlSavingSettlement comTransDtlSavingSettlement = comTransDtlSavingSettlementRepo.findById(transId).orElseThrow();
        comTransDtlSavingSettlement.setStatus(Constant.COM_STATUS_FAIL);
        comTransDtlSavingSettlementRepo.saveAndFlush(comTransDtlSavingSettlement);

        ComTransProcess comTransProcess = new ComTransProcess();
        comTransProcess.setSrvcCd(comTrans.getSrvcCd());
        comTransProcess.setTransId(comTrans.getId());
        comTransProcess.setStatus(Constant.COM_STATUS_FAIL);
        comTransProcessRepo.saveAndFlush(comTransProcess);
    }

    private void pushNotify(DepositClosureOutput depositClosureOutput, String token, ComTransDtlSavingSettlement comTransDtlSavingSettlement, CustInfo custInfo) {
        try {
            List<MessagesNotify> lstMessageNoti = new ArrayList<>();
            String msgId = Utility.getUUID();
            List<Receiver> listReceiverNoti = new ArrayList<>();

            Receiver receiverNotify = new Receiver();
            receiverNotify.setReceiverId(token);
            receiverNotify.setReceiverType("FCM_TOKEN");
            receiverNotify.setReceiverAppId("MOBILEAPP_CAM");
            receiverNotify.setReceiverUserId(custInfo.getUserId());
            listReceiverNoti.add(receiverNotify);

            String ccy = comTransDtlSavingSettlement.getCurrency();
            //Số ti�?n lãi tất toán trước hạn ( đã bao gồm thuế)
            String interestAmount = FormatNumber.formatAmount1(depositClosureOutput.getIntAmtPreClose(), ccy);
            //thuế
            String taxAmount = FormatNumber.formatAmount1(depositClosureOutput.getTaxAmount(), ccy);
            //Số ti�?n User nhận được = Principal amount + Net interest amount- Recover interest amount
            String receivingAmount = FormatNumber.formatAmount1(comTransDtlSavingSettlement.getReceivingAmount(), ccy);
            //Số ti�?n gốc của sổ
            String principalAmount = FormatNumber.formatAmount1(comTransDtlSavingSettlement.getSavingAmount(), ccy);

            BodyDataExtendParam dataParam = new BodyDataExtendParam();
            dataParam.setDataKey("content-type");
            dataParam.setDataValue("0");

            BodyDataExtendParam dataParam2 = new BodyDataExtendParam();
            dataParam2.setDataKey("body");
            dataParam2.setDataValue(this.notiContentBuilder(interestAmount, principalAmount, taxAmount, receivingAmount, depositClosureOutput, comTransDtlSavingSettlement, false));

            List<BodyDataExtendParam> lstDataParam = new ArrayList<>();
            lstDataParam.add(dataParam);
            lstDataParam.add(dataParam2);

            MessagesNotify messagesNotify = new MessagesNotify();
            messagesNotify.setBodyTitle("Your Fixed Deposit Successfully Closed Pre-Maturity");
            messagesNotify.setBodyContent(this.notiContentBuilder(interestAmount, principalAmount, taxAmount, receivingAmount, depositClosureOutput, comTransDtlSavingSettlement, true));
            messagesNotify.setBodyShortContent(this.notiContentBuilder(interestAmount, principalAmount, taxAmount, receivingAmount, depositClosureOutput, comTransDtlSavingSettlement, true));
            messagesNotify.setListReceiver(listReceiverNoti);
            messagesNotify.setBodyListDataExtendParam(lstDataParam);
            messagesNotify.setMessageType(SavingFixedDepositConfig.MSG_TYPE);
            messagesNotify.setMessageId(msgId);
            lstMessageNoti.add(messagesNotify);

            MessagesInput smsMessagesInput = new MessagesInput();
            smsMessagesInput.setListMessages(lstMessageNoti);

            ExecuteT24Output<NotifyOutput> output = callPushMessages
                    .pushNotifyAndSmsLuckeyMoney(smsMessagesInput, custInfo.getId(), Utility.getUUID());
            if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                AppLog.info("DoDepositClosure pushNoti fail: " + output.getSoaErrorCode() + " - "
                        + output.getSoaErrorDesc());
            }
        } catch (Exception e) {
            AppLog.error("DoDepositClosure pushNoti exception : " + e.getMessage());
        }

    }

    private String notiContentBuilder(String interestAmount, String principalAmount, String taxAmount, String receivingAmount, DepositClosureOutput depositClosureOutput, ComTransDtlSavingSettlement comTransDtlSavingSettlement, boolean isShortContent) {
        String breakLine = isShortContent ? ". " : "<br>";

        String interestOption = SavingProductEnum.MATURITY.getName().equals(comTransDtlSavingSettlement.getInterestOption())
                ? "Interest at Maturity"
                : comTransDtlSavingSettlement.getInterestOption();

        StringBuilder content = new StringBuilder("Transaction time: " + DateUtil.convertToDateString(new Date(), DateUtil.DATETIME_WITH_SLASH) + breakLine);
        content.append("Fixed deposit account: " + comTransDtlSavingSettlement.getSavingAcctNo() + breakLine);

        ////old version
        if (StringUtils.isNotEmpty(interestOption)) {
            content.append("Interest option: " + interestOption + breakLine);
        }

        if (SavingProductEnum.UPFRONT.getName().equals(comTransDtlSavingSettlement.getInterestOption())) {
            //todo
//            content.append("Upfront amount: " + interestAmount + " " + comTransDtlSavingSettlement.getCurrency() + breakLine);
        }
        content.append("Principal amount: " + principalAmount + " " + comTransDtlSavingSettlement.getCurrency() + breakLine);
        if (StringUtils.isNotEmpty(interestOption)  //old version
                || SavingProductEnum.MATURITY.getName().equals(comTransDtlSavingSettlement.getInterestOption())) {
            content.append("Interest amount: " + interestAmount + " " + comTransDtlSavingSettlement.getCurrency() + breakLine);
            content.append("Tax amount: " + taxAmount + " " + comTransDtlSavingSettlement.getCurrency() + breakLine);
        }
        if (SavingProductEnum.MONTHLY.getName().equals(comTransDtlSavingSettlement.getInterestOption())) {
            //So tien lai tat toan truoc han ( đa bao gom thue)
            content.append("Net Interest amount: " + interestAmount + " " + comTransDtlSavingSettlement.getCurrency() + breakLine);
            //Lai đa tra hang thang ( đa tru thue )
            content.append("Return Interest amount: " + FormatNumber.formatAmount1(depositClosureOutput.getIntCorr(), depositClosureOutput.getCurrency()) + " " + depositClosureOutput.getCurrency() + breakLine);
            content.append("Total receiving amount: " + FormatNumber.formatAmount1(depositClosureOutput.getTotAmtDue(), depositClosureOutput.getCurrency()) + " " + depositClosureOutput.getCurrency() + breakLine);
        } else {
            content.append("Total receiving amount: " + receivingAmount + " " + comTransDtlSavingSettlement.getCurrency() + breakLine);
        }
        content.append("Receiving account: " + comTransDtlSavingSettlement.getReceivingAccount() + breakLine);
        content.append("For any assistance, please contact MBCambodia's hotline at (+855)23968 686. Thank you.");
        return content.toString();
    }

    private void preparePartnerNotify(ProcessContext context, ComTransDtlSavingSettlement saving) {
        CustInfo custInfo = context.getCustomer();
        ComPartnerNotify notify = ComPartnerNotify.builder()
                .serviceName(context.getRequest().getSrvcCd())
                .custId(custInfo.getHostCifId())
                .amount(saving.getSavingAmount())
                .currency(saving.getCurrency())
                .transactionDate(new Date())
                .mbcTransaction(JSON.stringify(saving))
                .build();
        context.putVar("PARTNER_NOTIFY", notify);
    }
}
