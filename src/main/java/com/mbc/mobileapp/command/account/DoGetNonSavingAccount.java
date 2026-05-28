
package com.mbc.mobileapp.command.account;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.Acct;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.AcctRepo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.services.il.nonsavingacct.ProductInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.process.SynCurrentAccountProcess;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DoGetNonSavingAccount implements Command {

    @Autowired
    private ApiCustomer apiCustomer;
    
    @Autowired
    private AcctRepo acctRepo;
    
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String custId = customer.getId();
        String requestId = request.getRequestId();

        try {

            NonSavingAcctInput nonSavingAccountInput = new NonSavingAcctInput();
            nonSavingAccountInput.setAccountId(request.getSourceAccountNumber());
            nonSavingAccountInput.setCustomerId(customer.getHostCifId());           

            ExecuteT24Output<List<AccountBase>> esbOutput =
                apiCustomer.getNonSavingAccountListOtherSalary(nonSavingAccountInput, custId, requestId);
            
//            ExecuteT24Output<List<AccountBase>> esbOutput = new ExecuteT24Routine(ConstantUrl.EXECUTE_T24_ROUTINE)
//                .getNonSavingAccountList(nonSavingAccountInput, custId, requestId);

            if (esbOutput != null) {
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(esbOutput.getStatus())) {
                    List<AccountBase> acct = new ArrayList<AccountBase>();
                    for (AccountBase accountBase : esbOutput.getData()) {

                        ProductInfo productInfo = accountBase.getProductInfoMap().get("Category");
                        if (Constant.ACCOUNT_TYPE_CURRENT.equals(productInfo.getId()) ||
                            Constant.ACCOUNT_TYPE_SALARY.equals(productInfo.getId())) {   
                            Acct acctObj = acctRepo.findByAcctNo(accountBase.getAcctId());
                            // dong bo du lieu tu core ve DB app
                            if (acctObj != null) {
                                accountBase.setIsNotify(acctObj.getIsNotify());
                                accountBase.setIsDefault(acctObj.getIsDefault());
                            }
                            acct.add(accountBase);                                                                                
                        }
                    }
                   
                    SynCurrentAccountProcess synCurrentAccountProcess = applicationContext.getBean(SynCurrentAccountProcess.class);
                    synCurrentAccountProcess.setName("SynCurrentAccountProcess");
                    synCurrentAccountProcess.setData(customer, acct);
                    synCurrentAccountProcess.start();
                    esbOutput.setData(acct);
                    response.setLstNonSavingAccount(esbOutput.getData());
                }
                else {
                    result = new SimpleResult(
                        esbOutput.getErrorInfo().getErrorDesc() + " - " + esbOutput.getErrorInfo().getErrorDetail(),
                        false, esbOutput.getErrorInfo().getErrorCode());
                }
            }
            else {
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
            }

        }
        catch (Exception e) {
            AppLog.error("[SDK Exception Get List Account]: requestId: "+requestId+", desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

}
