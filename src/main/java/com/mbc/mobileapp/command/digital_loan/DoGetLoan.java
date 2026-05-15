package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.loanorigination.DoGenT24DayNowOutput;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.ApiMsLoan;
import com.mbc.mobileapp.api.model.digitalloan.output.GetLoanOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoGetLoan implements Command {
    private final ApiMsLoan apiMsLoan;
    private final ApiCustomer apiCustomer;

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();
        String custId = custInfo.getId();
        ExecuteT24Output<DoGenT24DayNowOutput> T24DayNowOutput;

        String ldId = null;
        String accountNo = null;
        if (request.getGetLoanRequest() != null) {
            ldId = request.getGetLoanRequest().getLdId();
            accountNo = request.getGetLoanRequest().getAccountNo();
        }

        try {
            ExecuteT24Output<GetLoanOutput> executeT24Output = apiMsLoan.getLoan(custInfo.getHostCifId(), ldId, accountNo, custId, request.getRequestId());
            if (Objects.isNull(executeT24Output)) {
                //get loan timeout
                log.info("[DoGetLoan] timeout");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
            } else {
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(executeT24Output.getStatus())) {
                    GetLoanOutput getLoan = executeT24Output.getData();
                    T24DayNowOutput = apiCustomer.genT24DayNow(custId, request.getRequestId());
                    if (Objects.isNull(T24DayNowOutput)) {
                        result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    } else {
                        if (Constant.CALL_MICROSERVICE_SUCCESS.equals(T24DayNowOutput.getStatus())) {
                            response.setT24DayNow(T24DayNowOutput.getData().getToday());
                        }
                    }
                    response.setLoanOutput(getLoan);
                    context.setResponse(response);
                } else {
                    //get loan error
                    String errorDesc = executeT24Output.getErrorInfo().getErrorDesc();
                    if (!Utility.isNull(executeT24Output.getErrorInfo().getErrorDetail())) {
                        errorDesc = executeT24Output.getErrorInfo().getErrorDesc() + " - " + executeT24Output.getErrorInfo().getErrorDetail();
                    }
                    log.info("[DoGetLoan] error {}", errorDesc);
                    result = new SimpleResult(errorDesc, false, executeT24Output.getErrorInfo().getErrorCode());
                }
            }
        } catch (Exception e) {
            log.error("[EXCEPTION GET LOAN] requestId: {}, desc: ", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
