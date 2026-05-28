package com.mbc.mobileapp.command.account;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallHistoryTransactionService;
import com.mbc.mobileapp.api.model.account.transaction.history.bakong.GetDetailTransactionHistoryInput;
import com.mbc.mobileapp.api.model.account.transaction.history.bakong.GetDetailTransactionHistoryOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class DoGetDetailHistoryBakongGW implements Command {

    @Autowired
    private CallHistoryTransactionService callHistoryTransactionService;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext context_ = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CustInfo customer = context_.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) context_.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context_.getResponse();

        try {
//            GetDetailTransactionHistoryInput getDetailTransactionHistoryInput = new GetDetailTransactionHistoryInput();
//            getDetailTransactionHistoryInput.setT24RefId(request.getTransCode().replace("\\BNK", ""));
//            ExecuteT24Output<GetDetailTransactionHistoryOutput> outputExecuteT24Output
//                    = transactionHistoryBakongGwApi.getDetailTransactionHistoryOutputBakongGw(getDetailTransactionHistoryInput, customer.getId(), request.getRequestId());
//            if (Objects.nonNull(outputExecuteT24Output)) {
//                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(outputExecuteT24Output.getStatus())) {
//                    response.setGetDetailTransactionHistoryOutput(outputExecuteT24Output.getData());
//                } else {
//                    result = new SimpleResult(outputExecuteT24Output.getErrorInfo().getErrorDesc(), false,
//                            outputExecuteT24Output.getErrorInfo().getErrorCode());
//                }
//            } else {
//                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
//                        ResponseCode.REQUEST_TIMEOUT.getCode());
//            }
            
            
            GetDetailTransactionHistoryInput getDetailTransactionHistoryInput = new GetDetailTransactionHistoryInput();
            getDetailTransactionHistoryInput.setT24RefId(request.getTransCode().replace("\\BNK", ""));
            ExecuteT24Output<GetDetailTransactionHistoryOutput> outputExecuteT24Output
                    = callHistoryTransactionService.getCiftpDetailHistoryTransaction(getDetailTransactionHistoryInput, customer.getId(), request.getRequestId());
            if (Objects.nonNull(outputExecuteT24Output)) {
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(outputExecuteT24Output.getStatus())) {
                    response.setGetDetailTransactionHistoryOutput(outputExecuteT24Output.getData());
                } else {
                    result = new SimpleResult(outputExecuteT24Output.getErrorInfo().getErrorDesc(), false,
                            outputExecuteT24Output.getErrorInfo().getErrorCode());
                }
            } else {
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                        ResponseCode.REQUEST_TIMEOUT.getCode());
            }
            
        } catch (Exception e) {
            log.error("[EXCEPTION CIFTP GET DETAIL HISTORY] requestId: {}, desc: {} ", request.getRequestId(), e.getStackTrace());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());

        }
        context_.setResult(result);
        return !result.isOk();
    }
}
