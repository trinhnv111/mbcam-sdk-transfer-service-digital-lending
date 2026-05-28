package com.mbc.mobileapp.command.transfer;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallFundsTransferService;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpBankInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoCiftpGetListBank implements Command {

    @Autowired
    private CallFundsTransferService callFundsTransferService;

    @Autowired
    private Environment env;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        try {   
            String bankList = RedisServer.getCacheRedis(RedisServer.CIFTP_GET_BANK_LIST);
            List<CiftpBankInfo> ciftpListBank = new ArrayList<CiftpBankInfo>();
            if (!Utility.isNull(bankList)) {               
                ciftpListBank = Arrays.asList(JSON.parseObject(bankList, CiftpBankInfo[].class));
//                RedisServer.saveCacheRedis(RedisServer.CIFTP_GET_BANK_LIST, dataRate, 10);
                
            }else {
                ExecuteT24Output<List<CiftpBankInfo>> output =
                        callFundsTransferService.getListBankCiftp(env.getProperty("provider.domestic.transfer.ciftp"),
                        customer.getId(), request.getRequestId());
                
                if(Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                    ciftpListBank
                            =  output.getData().stream().filter(bank->!"MB Bank".equals(bank.getBankName()))
                            .sorted(Comparator.comparing(CiftpBankInfo::getBankName))
                            .collect(Collectors.toList());

                    RedisServer.saveCacheRedis(RedisServer.CIFTP_GET_BANK_LIST, JSON.stringify(ciftpListBank), 15);

                }else {
                    String errorDesc = output.getErrorInfo().getErrorDesc();
                    if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                        errorDesc =
                                output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                    }
                    result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());
                }
            }
            response.setLstCiftpBank(ciftpListBank);
            
        }
        catch (Exception e) {
            AppLog.error("[SDK Exception Get Bank List CIFTP] requestId: "+request.getRequestId()+" desc: " , e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
