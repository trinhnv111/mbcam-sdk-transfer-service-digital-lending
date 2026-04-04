package com.mbc.mobileapp.command.transfer;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.services.il.nonsavingacct.PostingRestrict;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DoCheckCreditAccount implements Command {
    
    @Autowired
    private ApiCustomer apiCustomer;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo customer = context.getCustomer();
        
        TransInfo transInfo = request.getTransInfo();

        String custId = customer.getId();
        String requestId = request.getRequestId();
        try {
            String response_code = "";
            String response_msg = "";
            String response_dtl = "";
            String response_status = "";
            
            NonSavingAcctInput inputMessage = new NonSavingAcctInput();
            inputMessage.setAccountId(transInfo.getCreditAcctNo());
            ExecuteT24Output<List<AccountBase>> nonAccOutput =
                apiCustomer.getNonSavingAccountList(inputMessage, custId, requestId);
            
            if (nonAccOutput != null) {
                response_status = nonAccOutput.getStatus();
                
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(response_status)) {
                    if (nonAccOutput.getData().size() > 0) {
                        AccountBase account = nonAccOutput.getData().get(0);

                        if(!account.getAcctnCurrency().equals(transInfo.getCreditCurrency())){
                            result = new SimpleResult(MBCResponseCode.CURRENCY_INVALID.getDesc(), false,
                                    MBCResponseCode.CURRENCY_INVALID.getCode());
                        }

                        if(!account.getCustName().equals(transInfo.getCreditAcctName())) {
                            result = new SimpleResult(MBCResponseCode.CIFTP_VALID_INFO_BENE.getDesc(), false,
                                MBCResponseCode.CIFTP_VALID_INFO_BENE.getCode());
                        }
                        
                        for (PostingRestrict postingRestrict : account.getPostingRestrictList()) {
                            if("2".equals(postingRestrict.getId()) ||
                                "3".equals(postingRestrict.getId())) {
                                result = new SimpleResult(ResponseCode.CREDIT_ACCOUNT_INVALID.getDesc(), false,
                                    ResponseCode.CREDIT_ACCOUNT_INVALID.getCode());
                                break;
                            }
                        }
                    }
                }
                else {
                    response_code = nonAccOutput.getErrorInfo().getErrorCode();
                    response_msg = nonAccOutput.getErrorInfo().getErrorDesc();
                    response_dtl = nonAccOutput.getErrorInfo().getErrorDetail();
                    result = new SimpleResult(response_msg + " - " + response_dtl, false, response_code);
                }
            }
            else {
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
            }
            
        }catch (Exception e) {
            log.error("[Exception Validate Transfer] requestId: {}, data: {} ", requestId, e.getStackTrace());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

}
