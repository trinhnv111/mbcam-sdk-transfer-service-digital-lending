package com.mbc.mobileapp.command.saving.open;

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
import com.mbc.mobileapp.api.CallMsILService;
import com.mbc.mobileapp.api.CallSavingService;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.api.model.saving.account.SavingAccountListInput;
import com.mbc.mobileapp.api.model.saving.open.InterestPaymentInterval;
import com.mbc.mobileapp.api.model.saving.open.OpenSavingInput;
import com.mbc.mobileapp.api.model.saving.open.OpenSavingOutput;
import com.mbc.mobileapp.config.SavingFixedDepositConfig;
import com.mbc.mobileapp.config.SavingFlexiDepositConfig;
import com.mbc.mobileapp.config.SavingRealTimeDepositConfig;
import com.mbc.mobileapp.constant.SavingDepositConstant;
import com.mbc.mobileapp.constant.SavingProductEnum;
import com.mbc.mobileapp.constant.format.FormatNumber;
import com.mbc.mobileapp.repository.ComCampaignConfigExtendRepo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import jodd.util.StringUtil;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class DoOpenSaving implements Command {

    @Autowired
    private CallSavingService apiSavingFixedDeposit;

    @Autowired
    private CallMsILService callMsILService;

    @Autowired
    private ComTransRepo comTransRepo;

    @Autowired
    private ComTransDtlSavingRepo comTransDtlSavingRepo;

    @Autowired
    private ComTransProcessRepo comTransProcessRepo;

    @Autowired
    private CallPushMessages callPushMessages;

    @Autowired
    private ComMobileDeviceRepo comMobileDeviceRepo;

    @Autowired
    private ComCampaignConfigExtendRepo comCampaignConfigExtendRepo;

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
                setStatusTrans(transId, result.getMessage(), result.getResponseCode());
                context.setResult(result);
                return !result.isOk();
            }

            comTrans.setAuthMethod(Objects.nonNull(otp.getAuthMethod()) ? otp.getAuthMethod() : TransactionAuthMethod.AUTH_METHOD_SMS);
            //lấy detail
            ComTransDtlSaving comTransDtlSaving = comTransDtlSavingRepo.findByIdAndStatus(comTrans.getId(), Constant.COM_STATUS_INT);
            if (Objects.isNull(comTransDtlSaving)) {
                result = new SimpleResult(MBCResponseCode.TRANSACTION_NOT_FOUND.getDesc(), false,
                        MBCResponseCode.TRANSACTION_NOT_FOUND.getCode());
                setStatusTrans(transId, result.getMessage(), result.getResponseCode());
                context.setResult(result);
                return !result.isOk();
            }
            comTransDtlSaving.setRequestId(requestId);

            //check volume campaign
            if (Objects.nonNull(comTransDtlSaving.getCampaignCode())) {
                List<ComCampaignConfig> lstCampaign = comCampaignConfigExtendRepo.getListCampaignByCampaignCode(comTransDtlSaving.getCampaignCode(), "DEPOSIT");
                if (Objects.nonNull(lstCampaign.get(0).getVolume())) {
                    int numberApply = comTransDtlSavingRepo.numberOfCampaignCodeApply(comTransDtlSaving.getCampaignCode()) + 1;
                    if (Double.valueOf(lstCampaign.get(0).getVolume()) < numberApply) {

                        setStatusTrans(transId, "Max Volume Apply Campaign",
                                MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getCode());
                        result = new SimpleResult(MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getDesc(), false,
                                MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }
                }
            }

            String rolloverIntRate = null;
            String savingTitle = "";
            InterestPaymentInterval interestPaymentInterval;
            String maturityInstructions = comTransDtlSaving.getDisburseForm().substring(0, comTransDtlSaving.getDisburseForm().indexOf("-"));

            if (Constant.SrvcCd.SRVC_SAVING_FIXED_DEPOSIT.equals(request.getSrvcCd())) {
//                savingTitle ="FIXED_DEPOSIT_ACCOUNT";
                savingTitle = null;
                if (SavingProductEnum.MATURITY.getProductCode().equals(comTransDtlSaving.getProductCode())) {
                    rolloverIntRate = "2,3".indexOf(maturityInstructions) != -1 ? "P09" : null;
                }
                if (SavingProductEnum.MONTHLY.getProductCode().equals(comTransDtlSaving.getProductCode())) {
                    rolloverIntRate = "2,3".indexOf(maturityInstructions) != -1 ? "P11" : null;
                }

                interestPaymentInterval = InterestPaymentInterval.builder()
                        .interestPaymentIntervalPeriod(SavingProductEnum.MONTHLY.getProductCode().equals(comTransDtlSaving.getProductCode())
                                ? "MONTHLYONDATE"
                                : SavingDepositConstant.SavingDepositType.INTEREST_PAYMENT_PERIOD)
                        .build();
            } else {
                interestPaymentInterval = InterestPaymentInterval.builder()
                        .interestPaymentIntervalPeriod(SavingDepositConstant.SavingDepositType.INTEREST_PAYMENT_PERIOD)
                        .build();

                if (SavingRealTimeDepositConfig.PRODUCT_CODE.equals(comTransDtlSaving.getProductCode())) {
//                    savingTitle = "REAL_TIME_DEPOSIT_ACCOUNT";
                    savingTitle = null;
                    rolloverIntRate = SavingRealTimeDepositConfig.getInterestRollOverKey(maturityInstructions);
                }

                if (SavingFlexiDepositConfig.PRODUCT_CODE.equals(comTransDtlSaving.getProductCode())) {
                    savingTitle = "FLEXI_TERM_DEPOSIT_ACCOUNT";
                    rolloverIntRate = SavingFlexiDepositConfig.getInterestRollOverKey(maturityInstructions);
                }
            }

            OpenSavingInput input = OpenSavingInput.builder()
                    .customer(customer.getHostCifId())
                    .repayAccount(comTransDtlSaving.getDebitAcctNo())
                    .repayAccountName(comTransDtlSaving.getDebitAcctName())
                    .repayAccountType(SavingDepositConstant.AccountType.ACCOUNT)
                    .repayAccountCurrency(comTransDtlSaving.getDebitCurrency())
                    .currency(comTransDtlSaving.getDebitCurrency())
                    .principalAmt(comTransDtlSaving.getDebitAmount())
                    .locTerm(comTransDtlSaving.getPeriod())
                    .interestRate(comTransDtlSaving.getInterest().toString())
                    .maturityInstr(Integer.valueOf(maturityInstructions))
                    .interestPaymentInterval(interestPaymentInterval)
                    .channel(comTrans.getChannel())
                    .coCode(comTransDtlSaving.getBranchCode())
                    .mbLdType(StringUtil.isEmpty(comTransDtlSaving.getCampaignCode()) ? "0000000" : comTransDtlSaving.getCampaignCode())
                    .productGrCode("0000000")
                    .productCode(comTransDtlSaving.getProductCode())
                    .nominateAccount(comTransDtlSaving.getBeneAccount())
                    .maturityFromCache(true)
                    .requestId(comTransDtlSaving.getId())
                    .accountTitle(savingTitle)
                    .rolloverIntRate(rolloverIntRate)
                    .refRmCode(comTransDtlSaving.getReferrerPhone())
                    .refPartCode(comTransDtlSaving.getPartnerCode())
                    .build();

            setProcessingTrans(transId);
            ExecuteT24Output<OpenSavingOutput> output = apiSavingFixedDeposit.openSavingFixedDeposit(input, custId, requestId);
            if (Objects.isNull(output)) {
                AppLog.error(" DoOpenSavingFixedDeposit timeout ");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
                updateTrans(output, comTrans, comTransDtlSaving);
                context.setResult(result);
                return !result.isOk();
            }
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {

                OpenSavingOutput openSavingFixedDepositOutput = output.getData();
                openSavingFixedDepositOutput.setMaturityInstruction(comTransDtlSaving.getDisburseForm().substring(2));
                if (Constant.SrvcCd.SRVC_SAVING_FIXED_DEPOSIT.equals(request.getSrvcCd())) {
                    openSavingFixedDepositOutput.setNameAccountDeposit("FIXED_DEPOSIT_ACCOUNT");
                }
                if (Constant.SrvcCd.SRVC_SAVING_REAL_TIME.equals(request.getSrvcCd())) {
                    openSavingFixedDepositOutput.setNameAccountDeposit("REAL_TIME_DEPOSIT_ACCOUNT");
                }
                if (Constant.SrvcCd.SRVC_SAVING_FLEXI_TERM.equals(request.getSrvcCd())) {
                    openSavingFixedDepositOutput.setNameAccountDeposit("FLEXI_TERM_DEPOSIT_ACCOUNT");
                }

                response.setOpenSavingOutput(openSavingFixedDepositOutput);
                //update
                updateTrans(output, comTrans, comTransDtlSaving);
                context.setResult(result);
                context.setResponse(response);
                context.putVar(Constant.KeyVar.CURRENCY_TRANSACTION_EXECUTE, comTrans);

                //get token
                List<ComMobileDevice> tokens = comMobileDeviceRepo
                        .findByCustIdAndDeviceIdAndStatus(customer.getId(), request.getDeviceIdCommon(), Constant.STATUS_1);
                if (!tokens.isEmpty()) {
                    String token = tokens.get(0).getDeviceToken();
                    //push noti
                    ExecuteT24Output<List<AccountSaving>> savingAccOutput = callMsILService.getSavingAccountListV3(
                            new SavingAccountListInput(null, comTransDtlSaving.getSavingAcctNo(), null),
                            customer.getId(),
                            request.getRequestId());
                    if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(savingAccOutput.getStatus())) {
                        result = new SimpleResult(MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getDesc(), false,
                                MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getCode());
                    }
                    this.pushNotify(token, comTransDtlSaving, customer, requestId, savingAccOutput.getData().get(0));
                    this.preparePartnerNotify(context, comTransDtlSaving);
                }

            } else {
                if ("002".equals(output.getSoaErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.OPEN_SAVINGS_TIMEOUT.getDesc(), false,
                            MBCResponseCode.OPEN_SAVINGS_TIMEOUT.getCode());
                } else {
                    result = new SimpleResult(MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getDesc(), false,
                            MBCResponseCode.OPEN_SAVING_DEPOSIT_FAIL.getCode());

                }
                updateTrans(output, comTrans, comTransDtlSaving);
                context.setResult(result);
            }
            return !result.isOk();

        } catch (Exception e) {
            AppLog.error("[SDK EXCEPTION OPEN SAVING DEPOSIT] requestId: " + requestId + " desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
            setStatusTrans(transId, result.getMessage(), result.getResponseCode());
            context.setResult(result);
            return !result.isOk();
        }
    }

    private void setStatusTrans(String transId, String errDesc, String errCode) {
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
        comTransProcess.setErrorDesc(errDesc);
        comTransProcess.setErrorCode(errCode);
        comTransProcessRepo.saveAndFlush(comTransProcess);
    }

    private void setProcessingTrans(String transId) {
        ComTrans comTrans = comTransRepo.findById(transId).orElseThrow();
        comTrans.setStatus(Constant.COM_STATUS_PRO);
        comTransRepo.saveAndFlush(comTrans);

        ComTransDtlSaving comTransDtlSaving = comTransDtlSavingRepo.findById(transId).orElseThrow();
        comTransDtlSaving.setStatus(Constant.COM_STATUS_PRO);
        comTransDtlSavingRepo.saveAndFlush(comTransDtlSaving);

        ComTransProcess comTransProcess = new ComTransProcess();
        comTransProcess.setSrvcCd(comTrans.getSrvcCd());
        comTransProcess.setTransId(comTrans.getId());
        comTransProcess.setStatus(Constant.COM_STATUS_PRO);
        comTransProcessRepo.saveAndFlush(comTransProcess);
    }

    private void updateTrans(ExecuteT24Output<OpenSavingOutput> executeT24Output, ComTrans transInfo, ComTransDtlSaving comTransDtlSaving) {
        if (Objects.nonNull(executeT24Output)) {
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(executeT24Output.getStatus())) {
                transInfo.setStatus(Constant.COM_STATUS_COM);
                transInfo = comTransRepo.saveAndFlush(transInfo);

                comTransDtlSaving.setStartDate(executeT24Output.getData().getValueDate());
                comTransDtlSaving.setDueDate(executeT24Output.getData().getMaturityDate());
                comTransDtlSaving.setSavingAcctNo(executeT24Output.getData().getT24VersionId());
                comTransDtlSaving.setSavingCurrency(comTransDtlSaving.getDebitCurrency());
//                comTransDtlSaving.setInterest(new BigDecimal(executeT24Output.getData().getInterestRate()));
                comTransDtlSaving.setSavingAcctName(SavingDepositConstant.SAVING_ACCOUNT_NAME.get(comTransDtlSaving.getProductCode()));
                comTransDtlSaving.setSavingAmount(executeT24Output.getData().getPrincipalAmt());
                comTransDtlSaving.setStatus(Constant.COM_STATUS_COM);
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


    private void pushNotify(String token, ComTransDtlSaving comTransDtlSaving, CustInfo custInfo, String requestId, AccountSaving t24AccSaving) {
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

            String term = comTransDtlSaving.getPeriod().endsWith("M") ? comTransDtlSaving.getPeriod().replace("M", " months") : comTransDtlSaving.getPeriod().replace("D", " days");
            term = "1 months".equals(term) ? term.replace("s", "") : term;

            String principalAmount = Constant.CURRENCY_TYPE_USD.equals(comTransDtlSaving.getSavingCurrency()) ? FormatNumber.formatAmountWithCommasTwoDecimal(comTransDtlSaving.getDebitAmount().toString()) : Utility.formatAmountWithCommas(comTransDtlSaving.getDebitAmount().toString());
            String smsContent = "Fixed deposit account: " + comTransDtlSaving.getSavingAcctNo() +
                    ". Principal amount: " + principalAmount +
                    " " +
                    comTransDtlSaving.getDebitCurrency() +
                    ". Term: " + term + ". Interest rate: " + comTransDtlSaving.getInterest() + "%/year";

            BodyDataExtendParam dataParam = new BodyDataExtendParam();
            dataParam.setDataKey("content-type");
            dataParam.setDataValue("0");

            String amount = comTransDtlSaving.getSavingAmount().toString();
            if (Constant.CURRENCY_TYPE_USD.equals(comTransDtlSaving.getDebitCurrency())) {
                amount = FormatNumber.formatAmountWithCommasTwoDecimal(amount);
            } else {
                amount = Utility.formatAmountWithCommas(amount);
            }

            SavingProductEnum interestOption = SavingProductEnum.getByCategory(t24AccSaving.getProductInfo().get(0).getId());
            String ie = SavingProductEnum.MATURITY.getName().equals(interestOption.getName()) ? "Interest at Maturity" : interestOption.getName();

            StringBuilder notiContent = new StringBuilder("Transaction time: " + DateUtil.convertToDateString(new Date(), DateUtil.DATETIME_WITH_SLASH) + "<br>" +
                    "Fixed deposit account: " + comTransDtlSaving.getSavingAcctNo() + "<br>" +
                    "Source account: " + comTransDtlSaving.getDebitAcctNo() + "<br>" +
                    "Savings amount: " + amount + " " + comTransDtlSaving.getSavingCurrency() + "<br>" +
                    "Savings type: " + SavingFixedDepositConfig.SAVING_TYPE + "<br>" +
                    "Term deposit: " + term + "<br>" +
                    "Open date: " + DateUtil.convertToDateString(comTransDtlSaving.getStartDate(), DateUtil.DATE_WITH_SLASH) + "<br>" +
                    "Maturity date: " + DateUtil.convertToDateString(comTransDtlSaving.getDueDate(), DateUtil.DATE_WITH_SLASH) + "<br>" +
                    "Interest rate: " + comTransDtlSaving.getInterest() + "%/year <br>");
            if (StringUtils.isNotEmpty(ie))
                notiContent.append("Interest option: " + ie + "<br>");
            if (SavingProductEnum.MONTHLY.equals(interestOption)) {
                notiContent.append("Approx. Net monthly interest: " + FormatNumber.formatAmount1(new BigDecimal(t24AccSaving.getInterestInfo().getAmtIntEndCapDate()), t24AccSaving.getAccountCurrency()) + " " + t24AccSaving.getAccountCurrency() + "<br>");
            }
            if (SavingProductEnum.UPFRONT.equals(interestOption)) {
                notiContent.append("Upfront amount: " + t24AccSaving.getInterestInfo().getAmtIntEndCapDate() + " " + t24AccSaving.getAccountCurrency() + "<br>");
            }
            notiContent.append("Maturity option: " + comTransDtlSaving.getDisburseForm().substring(2) + "<br>" +
                    "Receiving account: " + comTransDtlSaving.getBeneAccount() + "<br>" +
                    "For any assistance, please contact MBCambodia's hotline at (+855)23968 686. Thank you.");

            BodyDataExtendParam dataParam2 = new BodyDataExtendParam();
            dataParam2.setDataKey("body");
            dataParam2.setDataValue(notiContent.toString());
            List<BodyDataExtendParam> lstDataParam = new ArrayList<>();
            lstDataParam.add(dataParam);
            lstDataParam.add(dataParam2);

            MessagesNotify messagesNotify = new MessagesNotify();
            messagesNotify.setBodyTitle("Fixed Deposit Successfully Opened");
            messagesNotify.setBodyContent(smsContent);
            messagesNotify.setBodyShortContent(smsContent);
            messagesNotify.setListReceiver(listReceiverNoti);
            messagesNotify.setBodyListDataExtendParam(lstDataParam);
            messagesNotify.setMessageType(SavingFixedDepositConfig.MSG_TYPE);
            messagesNotify.setMessageId(msgId);
            lstMessageNoti.add(messagesNotify);

            MessagesInput smsMessagesInput = new MessagesInput();
            smsMessagesInput.setListMessages(lstMessageNoti);

            ExecuteT24Output<NotifyOutput> output = callPushMessages
                    .pushNotifyAndSmsLuckeyMoney(smsMessagesInput, custInfo.getId(), requestId);
            if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                AppLog.info("DoOpenSavingFixedDeposit pushNoti fail: " + output.getSoaErrorCode() + " - "
                        + output.getSoaErrorDesc());
            }
        } catch (Exception e) {
            AppLog.error("DoOpenSavingFixedDeposit pushNoti exception : " + e.getMessage());
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
