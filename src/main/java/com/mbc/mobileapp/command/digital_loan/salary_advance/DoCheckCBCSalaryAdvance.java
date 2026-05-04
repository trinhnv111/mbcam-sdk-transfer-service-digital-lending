package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.ApiCBC;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.Constant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoCheckCBCSalaryAdvance implements Command {

    private final ApiCBC apiCBC;

    @Value("${api.cbc.requestBy}")
    private String cbcRequestBy;

    @Value("${api.cbc.appCode}")
    private String cbcAppCode;

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo custInfo = context.getCustomer();

        try {
            String clientMessageId = request.getRefNo();
            String clientUserId = custInfo.getUserId();
            String requestBy = cbcRequestBy;
            String appCode = cbcAppCode;
            String idNumber = custInfo.getIdTypNo();

            if (Utility.isNull(idNumber)) {
                log.error("[DoCheckCBCSalaryAdvance] idNumber is null");
                result = new SimpleResult("idNumber is required for CBC check", false, ResponseCode.INVALID_INPUT.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            ExecuteT24Output<Map<String, Object>> cbcResponse = apiCBC.getCbcData(clientMessageId, clientUserId, requestBy, appCode, Collections.singletonList(idNumber));

            if (cbcResponse != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(cbcResponse.getStatus())) {
                Map<String, Object> data = (Map<String, Object>) cbcResponse.getData().get("data");
                if (data != null) {
                    String statusCode = (String) data.get("statusCode");
                    if ("012".equals(statusCode) || "013".equals(statusCode) || "014".equals(statusCode)) {
                         log.error("[DoCheckCBCSalaryAdvance] CBC Report status invalid. statusCode: {}", statusCode);
                         result = new SimpleResult("CBC Check Failed", false, ResponseCode.TRANSACTION_FAIL.getCode());
                    } else {
                        String history12MStatus = (String) data.get("history12MStatus");
                        if (!Utility.isNull(history12MStatus)) {
                            history12MStatus = history12MStatus.toLowerCase();
                            if (!history12MStatus.equals("normal") && 
                                !history12MStatus.equals("closed") && 
                                !history12MStatus.equals("reject") && 
                                !history12MStatus.equals("no information")) {
                                log.error("[DoCheckCBCSalaryAdvance] Customer CBC status is invalid: {}", history12MStatus);
                                result = new SimpleResult("Customer has bad debt (CBC)", false, ResponseCode.TRANSACTION_FAIL.getCode());
                            }
                        }
                    }
                }
            } else {
                log.warn("[DoCheckCBCSalaryAdvance] Failed to get CBC data or status is not 200");
                result = new SimpleResult("Failed to verify CBC", false, ResponseCode.TRANSACTION_FAIL.getCode());
            }

        } catch (Exception e) {
            log.error("[DoCheckCBCSalaryAdvance] Exception: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }
}
