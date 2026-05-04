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
                Map<String, Object> data = cbcResponse.getData();
                if (data != null) {
                    String statusCode = (String) data.get("statusCode");
                    String statusDesc = (String) data.get("statusDesc");
                    log.info("[DoCheckCBCSalaryAdvance] CBC statusCode: {}, statusDesc: {}", statusCode, statusDesc);

                    if ("012".equals(statusCode) || "013".equals(statusCode) || "014".equals(statusCode)) {
                        // 012 = Error from CBC | 013 = Waiting approval | 014 = Declined → FAIL
                        log.error("[DoCheckCBCSalaryAdvance] CBC Report status invalid. statusCode: {}", statusCode);
                        result = new SimpleResult("CBC Check Failed - " + statusDesc, false, ResponseCode.TRANSACTION_FAIL.getCode());

                    } else if ("000".equals(statusCode)) {
                        // 000 = Success (có bản tin Effective) → check history12MStatus
                        String history12MStatus = (String) data.get("history12MStatus");
                        if (!Utility.isNull(history12MStatus)) {
                            history12MStatus = history12MStatus.toLowerCase();
                            if (!history12MStatus.equals("normal") &&
                                !history12MStatus.equals("closed") &&
                                !history12MStatus.equals("reject") &&
                                !history12MStatus.equals("no information")) {
                                log.error("[DoCheckCBCSalaryAdvance] Customer CBC debt status is invalid: {}", history12MStatus);
                                result = new SimpleResult("Customer has bad debt (CBC)", false, ResponseCode.TRANSACTION_FAIL.getCode());
                            }
                        }

                    } else {
                        // 011 = Expired | 015 = No report | other → PASS
                        log.info("[DoCheckCBCSalaryAdvance] CBC statusCode: {} — Pass (no blocking)", statusCode);
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
