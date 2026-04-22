package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.ApiMsLoan;
import com.mbc.mobileapp.api.model.digitalloan.output.PaymentHistoryOutPut;
import com.mbc.mobileapp.constant.CommonResponseCode;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.PaymentRequest;
import com.mbc.mobileapp.utils.Utilities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoGetPaymentHistory implements Command {

    private final ApiMsLoan apiMsLoan;

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        PaymentRequest req = request.getPaymentRequest();

        CustInfo custInfo = context.getCustomer();
        String custId = custInfo.getId();
        try {
            if (!Utilities.isValidDate(req.getFromDate(), "yyyyMMdd") || !Utilities.isValidDate(req.getToDate(), "yyyyMMdd")) {
                result = new SimpleResult(CommonResponseCode.DATE_FORMAT_INVALID.getErrorDesc(), false, CommonResponseCode.DATE_FORMAT_INVALID.getErrorCode());
                context.setResult(result);
                return !result.isOk();
            }
            ExecuteT24Output<List<PaymentHistoryOutPut>> executeT24Output = apiMsLoan.getPaymentHistory(req.getLoanId(), req.getFromDate(), req.getToDate(), custId, request.getRequestId());
            if (Objects.isNull(executeT24Output)) {
                //get history timeout
                log.info("[DoGetPaymentHistory] timeout");
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
            } else {
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(executeT24Output.getStatus())) {
                    List<PaymentHistoryOutPut> getHis = executeT24Output.getData();
                    response.setPaymentHistoryOutput(getHis);
                    context.setResponse(response);
                } else {
                    //get history error
                    String errorDesc = executeT24Output.getErrorInfo().getErrorDesc();
                    if (!Utility.isNull(executeT24Output.getErrorInfo().getErrorDetail())) {
                        errorDesc = executeT24Output.getErrorInfo().getErrorDesc() + " - " + executeT24Output.getErrorInfo().getErrorDetail();
                    }
                    log.info("[DoGetPaymentHistory] error {}", errorDesc);
                    result = new SimpleResult(errorDesc, false, executeT24Output.getErrorInfo().getErrorCode());
                }
            }

        } catch (Exception e) {
            log.error("[EXCEPTION GET HISTORY] requestId: {}, desc: ", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
