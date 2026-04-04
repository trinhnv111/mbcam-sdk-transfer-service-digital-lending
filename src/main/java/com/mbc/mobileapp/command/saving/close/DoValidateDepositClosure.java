package com.mbc.mobileapp.command.saving.close;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlSavingSettlement;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlSavingSettlementRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.services.il.nonsavingacct.PostingRestrict;
import com.mbc.common.services.il.nonsavingacct.ProductInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallMsILService;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.api.model.saving.account.SavingAccountListInput;
import com.mbc.mobileapp.constant.SavingDepositConstant;
import com.mbc.mobileapp.constant.SavingProductEnum;

import static com.mbc.mobileapp.constant.ChannelEnum.*;

import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.saving.close.DepositClosureInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DoValidateDepositClosure implements Command {

    private final ApiCustomer apiCustomer;

    private final CallMsILService callMsILService;

    private final ComTransRepo comTransRepo;

    private final ComTransProcessRepo comTransProcessRepo;

    private final ComTransDtlSavingSettlementRepo comTransDtlSavingSettlementRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        DepositClosureInfo closureInfo = request.getDepositClosureInfo();
        try {

            //check saving account number
            SavingAccountListInput inputSavingAccount = new SavingAccountListInput();
            inputSavingAccount.setAccountId(closureInfo.getSavingAcctNo());

            ExecuteT24Output<List<AccountSaving>> savingAccountNumber =
                    callMsILService.getSavingAccountListV3(inputSavingAccount, customer.getId(), request.getRequestId());
            if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(savingAccountNumber.getStatus())) {
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            AccountSaving accountSaving = savingAccountNumber.getData().get(0);
            if (!accountSaving.getCustomerId().equals(customer.getHostCifId())) {
                result = new SimpleResult(MBCResponseCode.CLOSE_SAVING_ACCT_NO_INVALID.getDesc(), false,
                        MBCResponseCode.CLOSE_SAVING_ACCT_NO_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            //CHECK CHANNEL
            if (!"SDK.RETAIL".equals(accountSaving.getChannel()) && !"SDK.RETAIL.EMONEY".equals(accountSaving.getChannel())) {
                result = new SimpleResult(MBCResponseCode.CLOSE_SAVING_ACCT_NO_INVALID.getDesc(), false,
                        MBCResponseCode.CLOSE_SAVING_ACCT_NO_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }

//                if(!CommonServiceConstant.CHANNEL_SAVING.get(request.getPartnerSdk()).equals(accountSaving.getChannel())) {
//                    result = new SimpleResult(MBCResponseCode.CLOSE_SAVING_ACCT_NO_INVALID.getDesc(), false,
//                        MBCResponseCode.CLOSE_SAVING_ACCT_NO_INVALID.getErrorCode());
//                    context.setResult(result);
//                    return !result.isOk();
//                }


            NonSavingAcctInput inputMessage = new NonSavingAcctInput();
            inputMessage.setCustomerId(customer.getHostCifId());
            ExecuteT24Output<List<AccountBase>> nonAccOutput =
                    apiCustomer.getNonSavingAccountList(inputMessage, customer.getId(), request.getRequestId());
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(nonAccOutput.getStatus())) {
                String[] postingRestrict = {"2", "3"};
                if (CollectionUtils.isEmpty(nonAccOutput.getData())) {
                    result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                            ResponseCode.TRANSACTION_FAIL.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                List<AccountBase> lstAccountBases = nonAccOutput.getData();
                AccountBase receivingAccountBase = null;
                for (AccountBase accountBase : lstAccountBases) {
                    if (closureInfo.getReceivingAccount().equals(accountBase.getAcctId()) &&
                            closureInfo.getReceivingCurrency().equals(accountBase.getAcctnCurrency())) {
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
                            receivingAccountBase = accountBase;

                            if (!receivingAccountBase.getAcctnCurrency().equals(closureInfo.getSavingCurrency()) ||
                                    !customer.getHostCifId().equals(receivingAccountBase.getCustId())) {
                                result = new SimpleResult(MBCResponseCode.RECEIVING_ACCOUNT_INVALID.getDesc(), false,
                                        MBCResponseCode.RECEIVING_ACCOUNT_INVALID.getCode());
                                context.setResult(result);
                                return !result.isOk();
                            }
                        }
                    }
                }
                if (Objects.isNull(receivingAccountBase)) {
                    result = new SimpleResult(MBCResponseCode.RECEIVING_ACCOUNT_INVALID.getDesc(), false,
                            MBCResponseCode.RECEIVING_ACCOUNT_INVALID.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
            } else {
                result = new SimpleResult(
                        nonAccOutput.getErrorInfo().getErrorDesc() + " - " + nonAccOutput.getErrorInfo().getErrorDetail(),
                        false, nonAccOutput.getErrorInfo().getErrorCode());
                context.setResult(result);
                return !result.isOk();
            }

            //create record
            comTransDtlSavingSettlementProcess(response, request, closureInfo, customer, accountSaving);
            context.setResponse(response);
            context.setResult(result);
            return !result.isOk();

        } catch (Exception e) {
            AppLog.error("ERROR", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }

    private void comTransDtlSavingSettlementProcess(
            CommonServiceResponse response,
            CommonServiceRequest request,
            DepositClosureInfo depositClosureInfo,
            CustInfo custInfo,
            AccountSaving accSaving
    ) {

        String savingName = "";
        if (Constant.SrvcCd.SRVC_SAVING_FIXED_DEPOSIT.equals(request.getSrvcCd())) {
            savingName = "FIXED_DEPOSIT_ACCOUNT";
        }
        if (Constant.SrvcCd.SRVC_SAVING_REAL_TIME.equals(request.getSrvcCd())) {
            savingName = "REAL_TIME_DEPOSIT_ACCOUNT";
        }
        if (Constant.SrvcCd.SRVC_SAVING_FLEXI_TERM.equals(request.getSrvcCd())) {
            savingName = "FLEXI_TERM_DEPOSIT_ACCOUNT";
        }

        ComTrans comTrans = new ComTrans();
        comTrans.setCustId(custInfo.getId());
        comTrans.setDebitAcctName(SavingDepositConstant.SavingDepositType.NAME_ACCOUNT_DEPOSIT);
        comTrans.setDebitCurrency(depositClosureInfo.getSavingCurrency());
        comTrans.setDebitAcctNo(depositClosureInfo.getSavingAcctNo());
        comTrans.setSrvcCd(request.getSrvcCd());
        comTrans.setBranchCode(depositClosureInfo.getBranchCode());
        comTrans.setCreatedBy(custInfo.getUserId());
        comTrans.setStatus(Constant.COM_STATUS_INT);
        comTrans.setChannel("M".equals(request.getDigitalChannel()) ? MOBILE_RETAIL.getCode() : request.getDigitalChannel());
        comTrans.setPartnerCode(accSaving.getPartner());
        comTrans.setSessionId(request.getSessionId());
        comTrans = comTransRepo.saveAndFlush(comTrans);


        ComTransDtlSavingSettlement comTransDtlSavingSettlement = ComTransDtlSavingSettlement.builder()
                .id(comTrans.getId())
                .transId(comTrans.getId())
                .branchCode(depositClosureInfo.getBranchCode())
                .createdBy(custInfo.getUserId())
                .custId(custInfo.getId())
                .savingType(depositClosureInfo.getSavingType())
                .interestOption(!Utility.isNull(depositClosureInfo.getInterestOption()) ? SavingProductEnum.getByCategory(depositClosureInfo.getInterestOption()).getName() : null)
                .savingAcctName(savingName)
                .savingAcctNo(depositClosureInfo.getSavingAcctNo())
                .receivingName(custInfo.getNm())
                .receivingAccount(depositClosureInfo.getReceivingAccount())
                .currency(depositClosureInfo.getReceivingCurrency())
                .requestId(depositClosureInfo.getRequestId())
                .channel("M".equals(request.getDigitalChannel()) ? MOBILE_RETAIL.getCode() : request.getDigitalChannel())
                .partnerCode(accSaving.getPartner())
                .status(Constant.COM_STATUS_INT).build();
        comTransDtlSavingSettlementRepo.saveAndFlush(comTransDtlSavingSettlement);

        ComTransProcess comTransProcess = new ComTransProcess();
        comTransProcess.setStatus(Constant.COM_STATUS_INT);
        comTransProcess.setTransId(comTrans.getId());
        comTransProcess.setSrvcCd(comTrans.getSrvcCd());
        comTransProcessRepo.saveAndFlush(comTransProcess);

        response.setTransId(comTrans.getId());

    }
}
