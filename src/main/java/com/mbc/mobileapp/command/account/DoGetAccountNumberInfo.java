
package com.mbc.mobileapp.command.account;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.il.base.ErrorInfo;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComSysParamRepo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.BranchInfo;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.SystemParameterConstants;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.account.AcctNumberInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DoGetAccountNumberInfo implements Command {

    private final ApiCustomer apiCustomer;

    private final ComSysParamRepo comSysParamRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {

        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String requestId = request.getRequestId();
        String[] accountNoSet = request.getAcctNo().split("\\.");
//        String customerNationalId = request.getCustomerNationId();

        try {
            List<String> ccyAcctActive = Arrays.asList(comSysParamRepo.findByCd(SystemParameterConstants.CURRENCY_ACCOUNT_ACTIVE).getValue().split(","));

            AccountBase t24Acc1 = this.handleGetT24Account(context, accountNoSet[0]);
            AccountBase t24Acc2 = this.handleGetT24Account(context, accountNoSet.length > 1 ? accountNoSet[1] : null);
//            ExecuteT24Output<List<AccountBase>> nonAccOutput = new ExecuteT24Routine(ConstantUrl.EXECUTE_T24_ROUTINE)
//                .getNonSavingAccountList(inputMessage, custId, requestId);
            //check valid currency and check 2 account same ccy
            if (!this.isValidCurrency(t24Acc1, ccyAcctActive) || !this.isValidCurrency(t24Acc2, ccyAcctActive)) {
                result = new SimpleResult(ResponseCode.CREDIT_ACCOUNT_CURRENCY_INVALID.getDesc(), false,
                        ResponseCode.CREDIT_ACCOUNT_CURRENCY_INVALID.getCode());
                context.setResponse(response);
                context.setResult(result);
                return !result.isOk();
            }
            if (Objects.nonNull(t24Acc1) && Objects.nonNull(t24Acc2)
                    && t24Acc1.getAcctnCurrency().equals(t24Acc2.getAcctnCurrency())) {
                result = new SimpleResult(MBCResponseCode.INVALID_ACCOUNT_PAIR_CURRENCY.getDesc(), false,
                        MBCResponseCode.INVALID_ACCOUNT_PAIR_CURRENCY.getCode());
                context.setResponse(response);
                context.setResult(result);
                return !result.isOk();
            }

            BranchInfo branch = new BranchInfo();
            branch.setName(t24Acc1.getBranchInfo().getName());
            branch.setCode(t24Acc1.getBranchInfo().getCode());
            branch.setMnemonic(t24Acc1.getBranchInfo().getMnemonic());

            AcctNumberInfo acctNo = AcctNumberInfo.builder()
                    .accountNo(accountNoSet[0])
                    .accountType(t24Acc1.getAcctnType())
                    .accountCurrency(t24Acc1.getAcctnCurrency())
                    .customerName(t24Acc1.getCustName())
                    .branchInfo(branch)
                    .custId(t24Acc1.getCustId())
                    .build();

            response.setAcctNumberInfo(acctNo);
            List<AcctNumberInfo> accountList = Stream.of(t24Acc1, t24Acc2)
                    .filter(Objects::nonNull)
                    .map(acc -> {
                        BranchInfo branchInfo = new BranchInfo();
                        branchInfo.setName(acc.getBranchInfo().getName());
                        branchInfo.setCode(acc.getBranchInfo().getCode());
                        branchInfo.setMnemonic(acc.getBranchInfo().getMnemonic());
                        return AcctNumberInfo.builder()
                                .accountNo(acc.getAcctId())
                                .customerName(acc.getCustName())
                                .branchInfo(acc.getBranchInfo())
                                .accountCurrency(acc.getAcctnCurrency())
                                .accountType(acc.getAcctnType())
                                .custId(acc.getCustId())
                                .build();
                    })
                    .collect(Collectors.toList());
            //check 2 account cung 1 cif
            if (accountList.size() == 2) {
                if (!t24Acc1.getCustId().equals(t24Acc2.getCustId())) {
                    result = new SimpleResult(ResponseCode.CREDIT_ACCOUNT_INVALID.getDesc(), false,
                            ResponseCode.CREDIT_ACCOUNT_INVALID.getCode());
                    context.setResponse(response);
                    context.setResult(result);
                    return !result.isOk();
                }
            }
            context.putVar("T24_SET_ACC", accountList);

        } catch (IllegalArgumentException ex) {
            if ("REQUEST_TIMEOUT".equals(ex.getMessage())) {
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResponse(response);
                context.setResult(result);
                return !result.isOk();
            }
            if ("REQUEST_ERROR".equals(ex.getMessage())) {
                ErrorInfo error = (ErrorInfo) context.getVar("ERROR_INFO");
                result = new SimpleResult(error.getErrorDesc() + " - " + error.getErrorDetail(), false, error.getErrorCode());
                context.setResponse(response);
                context.setResult(result);
                return !result.isOk();
            }
            throw ex;
        } catch (Exception e) {
            AppLog.error("[Exception query cust info by account no]: requestId: " + requestId + " ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

    private AccountBase handleGetT24Account(ProcessContext context, String accountNo) throws IOException {
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();

        if (StringUtils.isEmpty(accountNo))
            return null;

        NonSavingAcctInput inputMessage = new NonSavingAcctInput();
        inputMessage.setAccountId(accountNo);
        ExecuteT24Output<List<AccountBase>> t24Acc1 = apiCustomer.getNonSavingAccountList(inputMessage, customer.getId(), request.getRequestId());
        if (Objects.isNull(t24Acc1)) {
            throw new IllegalArgumentException("REQUEST_TIMEOUT");
        }
        if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(t24Acc1.getStatus())) {
            context.putVar("ERROR_INFO", t24Acc1.getErrorInfo());
            throw new IllegalArgumentException("REQUEST_ERROR");
        }
        if (t24Acc1.getData().isEmpty()) {
            throw new IllegalArgumentException("DATA_INVALID");
        }
        return t24Acc1.getData().get(0);
    }

    private boolean isValidCurrency(AccountBase acc, List<String> ccyAcctActive) {
        if (Objects.isNull(acc))
            return true;
        return ccyAcctActive.contains(acc.getAcctnCurrency().toUpperCase());
    }




//    @Autowired
//    private ApiCustomer apiCustomer;
//
//    @Autowired
//    private ComSysParamRepo comSysParamRepo;
//
//    @Override
//    public boolean execute(Context cntxt) throws Exception {
//
//        ProcessContext context = (ProcessContext) cntxt;
//        Validator.Result result = Validator.Result.OK;
//
//        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
//        CustInfo customer = context.getCustomer();
//        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
//
//        String custId = customer.getId();
//        String requestId = request.getRequestId();
//        String account_no = request.getAcctNo();
////        String customerNationalId = request.getCustomerNationId();
//
//        try {
//            List<String> ccyAcctActive = Arrays.asList(comSysParamRepo.findByCd(SystemParameterConstants.CURRENCY_ACCOUNT_ACTIVE).getValue().split(","));
//
//
//            String response_code = "";
//            String response_msg = "";
//            String response_dtl = "";
//            String response_status = "";
//
//            NonSavingAcctInput inputMessage = new NonSavingAcctInput();
//            inputMessage.setAccountId(account_no);
//            ExecuteT24Output<List<AccountBase>> nonAccOutput =
//                apiCustomer.getNonSavingAccountList(inputMessage, custId, requestId);
//
////            ExecuteT24Output<List<AccountBase>> nonAccOutput = new ExecuteT24Routine(ConstantUrl.EXECUTE_T24_ROUTINE)
////                .getNonSavingAccountList(inputMessage, custId, requestId);
//
//            if (nonAccOutput != null) {
//                response_status = nonAccOutput.getStatus();
//                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(response_status)) {
//                    if (nonAccOutput.getData().size() > 0) {
//                        AccountBase account = nonAccOutput.getData().get(0);
//
//                        if(ccyAcctActive.indexOf(account.getAcctnCurrency().toUpperCase()) != -1){
//                            AcctNumberInfo acctNo = new AcctNumberInfo();
//                            acctNo.setAccountNo(account_no);
//                            acctNo.setCustomerName(account.getCustName());
//                            acctNo.setBranchInfo(account.getBranchInfo());
//                            acctNo.setAccountCurrency(account.getAcctnCurrency());
//                            acctNo.setAccountType(account.getAcctnType());
//                            response.setAcctNumberInfo(acctNo);
//                        }else{
//                            result = new SimpleResult(ResponseCode.CREDIT_ACCOUNT_CURRENCY_INVALID.getDesc(), false,
//                                    ResponseCode.CREDIT_ACCOUNT_CURRENCY_INVALID.getCode());
//                        }
//
////                        CustomerInfoInput input = new CustomerInfoInput();
////                        input.setCustomerId(account.getCustId());
////                        input.setCustomerNationalId(customerNationalId);
////
////                        ExecuteT24Output<CustomerInfoT24> esb_output =
////                            apiCustomer.getCustomerInfo(input, custId, requestId);
////
////                        if (esb_output != null) {
////                            response_status = esb_output.getStatus();
////                            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(response_status)) {
////                                esb_output.getData().setAcctList(nonAccOutput.getData());
////                                List<AccountBase> lstAccouBases = esb_output.getData().getAcctList();
////                                CustInfoByAcctNo acctNo = new CustInfoByAcctNo();
////
////                                String custName = Utility.isNull(esb_output.getData().getCustomerName().getVnName())
////                                    ? esb_output.getData().getCustomerName().getShortName().trim()
////                                    : esb_output.getData().getCustomerName().getVnName().trim();
////
////                                for (AccountBase accountBase : lstAccouBases) {
////                                    if (account_no.equals(accountBase.getAcctId())) {
////                                        acctNo.setAccountNo(account_no);
////                                        acctNo.setCustomerName(custName);
////                                        acctNo.setBranchInfo(accountBase.getBranchInfo());
////                                        acctNo.setAccountCurrency(accountBase.getAcctnCurrency());
////                                        acctNo.setAccountType(accountBase.getAcctnType());
////                                        response.setCustInfoByAcctNo(acctNo);
////                                        break;
////                                    }
////                                }
////
////                            }
////                            else {
////                                response_code = esb_output.getErrorInfo().getErrorCode();
////                                response_msg = esb_output.getErrorInfo().getErrorDesc();
////                                response_dtl = esb_output.getErrorInfo().getErrorDetail();
////                                result = new SimpleResult(response_msg + " - " + response_dtl, false, response_code);
////                            }
////                        }
////                        else {
////                            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
////                                ResponseCode.TRANSACTION_FAIL.getCode());
////                        }
//                    }
//                }
//                else {
//                    response_code = nonAccOutput.getErrorInfo().getErrorCode();
//                    response_msg = nonAccOutput.getErrorInfo().getErrorDesc();
//                    response_dtl = nonAccOutput.getErrorInfo().getErrorDetail();
//                    result = new SimpleResult(response_msg + " - " + response_dtl, false, response_code);
//                }
//            }
//            else {
//                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
//                    ResponseCode.TRANSACTION_FAIL.getCode());
//            }
//
//        }
//        catch (Exception e) {
//            AppLog.error("[Exception query cust info by account no]: requestId: "+ requestId + " ", e);
//            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
//                ResponseCode.TRANSACTION_FAIL.getCode());
//        }
//
//        context.setResponse(response);
//        context.setResult(result);
//        return !result.isOk();
//    }
}
