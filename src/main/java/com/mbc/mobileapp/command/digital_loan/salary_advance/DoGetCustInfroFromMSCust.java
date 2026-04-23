package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.customerinfo.CustomerInfoInput;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor

public class DoGetCustInfroFromMSCust implements Command {
    // 3 lấy ra iphone address từ ms customer
    private final ApiCustomer apiCustormer;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest commonServiceRequest = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = commonServiceRequest.getCust();

        try {
            AppLog.info("[SA-INIT] Get custormer info from ms customer- requestId:{}" + commonServiceRequest.getRequestId());

            CustomerInfoInput customerInfoInput = new CustomerInfoInput();
            customerInfoInput.setCustomerId(custInfo.getHostCifId());

            ExecuteT24Output<CustomerInfoT24> customerInfoT24 = apiCustormer.getCustomerInfo(customerInfoInput, custInfo.getId(), commonServiceRequest.getRequestId());

            if (Objects.nonNull(customerInfoT24) && Constant.CALL_MICROSERVICE_SUCCESS.equals(customerInfoT24.getStatus())) {
                processContext.put("customerInfoMS", customerInfoT24.getData());
                AppLog.info("[SA-INIT] Get custormer info from ms customer success- requestId:{}" + commonServiceRequest.getRequestId());
            } else {
                AppLog.info("[SA-INIT] Get custormer info from ms customer fail- requestId:{}" + commonServiceRequest.getRequestId());
            }


        } catch (Exception e) {
            AppLog.info("[SA-INIT] Get custormer info from ms customer exception - requestId:{}" + commonServiceRequest.getRequestId());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());

        }

        processContext.setResult(result);

        return !result.isOk();

    }
}
