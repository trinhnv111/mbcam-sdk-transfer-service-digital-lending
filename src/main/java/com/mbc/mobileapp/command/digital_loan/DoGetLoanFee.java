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
import com.mbc.mobileapp.api.model.digitalloan.output.LdFeeData;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoGetLoanFee implements Command {

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

        try {
            ExecuteT24Output<LdFeeData> executeT24Output = apiMsLoan.getLdFee( custId, request.getRequestId());
            if (Objects.isNull(executeT24Output)) {
                //GetFee timeout
                log.info("[DoGetFee] timeout");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
            } else {
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(executeT24Output.getStatus())) {
                    LdFeeData getFee = executeT24Output.getData();

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
                    context.putVar("fee",getFee.getFeeValue());
                    response.setLdFeeData(getFee);
                    context.setResponse(response);
                } else {
                    //Get Fee error
                    String errorDesc = executeT24Output.getErrorInfo().getErrorDesc();
                    if (!Utility.isNull(executeT24Output.getErrorInfo().getErrorDetail())) {
                        errorDesc = executeT24Output.getErrorInfo().getErrorDesc() + " - " + executeT24Output.getErrorInfo().getErrorDetail();
                    }
                    log.info("[DoGetFee] error {}", errorDesc);
                    result = new SimpleResult(errorDesc, false, executeT24Output.getErrorInfo().getErrorCode());
                }
            }
        } catch (Exception e) {
            log.error("[EXCEPTION GET DoGetFee] requestId: {}, desc: ", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
