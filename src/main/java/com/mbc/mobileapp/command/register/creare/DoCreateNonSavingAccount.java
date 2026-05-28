
package com.mbc.mobileapp.command.register.creare;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.Acct;
import com.mbc.common.entity.ComOpenOnlAcct;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.AcctRepo;
import com.mbc.common.repository.ComOpenOnlAcctRepo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallMsILService;
import com.mbc.mobileapp.api.model.register.NonSavingAccount;
import com.mbc.mobileapp.api.model.register.NonSavingAcctDataOutput;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DoCreateNonSavingAccount implements Command {

    @Autowired
    private ComOpenOnlAcctRepo comOpeningAcctRepo;

    @Autowired
    private AcctRepo acctRepo;

    @Autowired
    private CallMsILService callMsILService;

    @Autowired
    private ApiCustomer apiCustomer;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        Validator.Result result = Validator.Result.OK;
        RegisterCustInfo openCustomerInfo = request.getRegisterCustInfo();
        CustInfo custInfo = context.getCustomer();

        try {
            ExecuteT24Output<List<AccountBase>> acct_output = new ExecuteT24Output<List<AccountBase>>();
            if (custInfo != null) {
                NonSavingAcctInput inputMessage = new NonSavingAcctInput();
                inputMessage.setCustomerId(custInfo.getHostCifId());
                acct_output =
                    apiCustomer.getNonSavingAccountListOtherSalary(inputMessage, custInfo.getId(), request.getRequestId());
                // acct_output = new ExecuteT24RoutineMicroservice().getNonSavingAccountList(custInfo.getId(),
                // request.getRequestId(), null, custInfo.getHostCifId(), "0001");
                if ("400".equals(acct_output.getStatus())) {
                    result = createNonSavingAccount(openCustomerInfo, custInfo, request, context);
                }
                else {
                    
                    boolean check_account = false;
                    for (AccountBase account : acct_output.getData()) {
                        if(Constant.ACCT_STATUS_ACTIVE.equals(account.getAcctnStatus())) {
                            check_account = true;
                            break;
                        }
                    }

                    if(check_account) {
                        result = new SimpleResult(MBCResponseCode.NON_SAVING_ACCOUNT_ALREADY_EXISTS.getDesc(), true,
                            MBCResponseCode.NON_SAVING_ACCOUNT_ALREADY_EXISTS.getCode());
                    }else {
                        result = createNonSavingAccount(openCustomerInfo, custInfo, request, context);
                    }
                }
            }

        }
        catch (Exception e) {
            AppLog.error("ERROR", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        context.setRequest(request);
        return !result.isOk();
    }

    
    private Validator.Result createNonSavingAccount(RegisterCustInfo openCustomerInfo, CustInfo custInfo, CommonServiceRequest request, ProcessContext context) {
        Validator.Result result = Validator.Result.OK;
        String response_code = "";
        String response_msg = "";
        String response_dtl = "";
        String response_status = "";

        if(openCustomerInfo.getCurrency() != null && !openCustomerInfo.getCurrency().isEmpty()) {
            
            for (int i = 0; i < openCustomerInfo.getCurrency().size(); i++) {
                String currency = openCustomerInfo.getCurrency().get(i);
                NonSavingAccount messageInput = new NonSavingAccount();
                messageInput.setCustomerId(custInfo.getHostCifId());
                messageInput.setAccountTitle(openCustomerInfo.getCustName());
                messageInput.setShortTitle(openCustomerInfo.getLastName());
                messageInput.setCategory("1001");
                messageInput.setLdType("0000000");
                messageInput.setProductGrCode("0000000");
                messageInput.setCurrency(currency);
                messageInput.setBranchCode(CommonServiceConstant.BRANCH_CODE_HO);
                messageInput.setChannel(request.getDigitalChannel());

                ExecuteT24Output<NonSavingAcctDataOutput> create_output =
                    callMsILService.createNonSavingAccount(messageInput, custInfo.getId(), request.getRequestId());

                response_status = create_output.getStatus();
                response_code = create_output.getErrorInfo().getErrorCode();
                response_msg = create_output.getErrorInfo().getErrorDesc();
                response_dtl = create_output.getErrorInfo().getErrorDetail();

                if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(create_output.getStatus())) {
                    result = new SimpleResult(response_msg + " - " + response_dtl, false, response_code);
                    request.setCreateAcct("Create account: " + response_msg + "-" + response_dtl);
                    break;
                }
                else {
                    Acct acct = new Acct();
                    acct.setAcctNo(create_output.getData().getAccountId());
                    acct.setVersion(BigDecimal.ZERO);
                    acct.setAcctNm(openCustomerInfo.getCustName());
                    acct.setAcctAlias(openCustomerInfo.getLastName());
                    acct.setAcctTypCd(Constant.ACCOUNT_TYPE_CURRENT_ACCOUNT);
                    acct.setCcyCd(currency);
                    acct.setCategory(Constant.ACCOUNT_CATEGORY_DEFINED);
                    acct.setHostCustId(custInfo.getHostCifId());
                    acct.setCreatedBy(Constant.CHANNEL_EMB);
                    acct.setCustId(custInfo.getId());
                    acct.setInactiveSts("0");
                    acct.setIsAccsEbanking(Constant.YES);
                    acct.setIsDebit(Constant.YES);
                    acct.setIsCrdt(Constant.YES);
                    acct.setIsInq(Constant.YES);
                    acct.setOrgUnitCd(Constant.BRANCH_CODE_HO);
                    acct.setIsNotify(Constant.NO);
                    acct.setIsDefault(Constant.NO);
                    acctRepo.save(acct);

                }

            }
            
            request.setCreateAcct("Create account: " + response_status + "-" + response_code);

            ComOpenOnlAcct openAcct = (ComOpenOnlAcct) context.getVar("ComOpeningAcct");
            openAcct.setOpenAcct(response_status);
            comOpeningAcctRepo.saveAndFlush(openAcct);
        }else {
            result = new SimpleResult(MBCResponseCode.OPEN_NON_SAVING_ACCOUNT_FAIL_BY_CURRENCY.getDesc(), true,
                MBCResponseCode.OPEN_NON_SAVING_ACCOUNT_FAIL_BY_CURRENCY.getCode());
        }
        
        return result;
    }
    
}
