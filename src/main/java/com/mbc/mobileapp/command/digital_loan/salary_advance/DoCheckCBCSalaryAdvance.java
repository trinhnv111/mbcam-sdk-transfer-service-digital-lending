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
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
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
            String idNumber = custInfo.getIdTypNo();

            if (Utility.isNull(idNumber)) {
                log.error("[DoCheckCBCSalaryAdvance] idNumber is null");
                result = new SimpleResult("idNumber is required for CBC check", false, ResponseCode.INVALID_INPUT.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            ExecuteT24Output<List<Map<String, Object>>> cbcResponse = apiCBC.getCbcData(
                    clientMessageId, clientUserId, cbcRequestBy, cbcAppCode,
                    Collections.singletonList(idNumber));

            if (cbcResponse == null || !Constant.CALL_MICROSERVICE_SUCCESS.equals(cbcResponse.getStatus())) {
                log.warn("[DoCheckCBCSalaryAdvance] CBC call failed or null response - requestId:{}", request.getRequestId());
                result = new SimpleResult("Failed to verify CBC", false, ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            Map<String, Object> data = CollectionUtils.isEmpty(cbcResponse.getData())
                    ? null
                    : cbcResponse.getData().get(0);

            String statusCode = data == null ? null : (String) data.get("statusCode");
            String statusDesc = data == null ? null : (String) data.get("statusDesc");

            String historyOneYear = data == null ? null : (String) data.get("historyOneYear");

            log.info("[DoCheckCBCSalaryAdvance] CBC result - requestId:{}, statusCode:{}, statusDesc:{}, historyOneYear:{}",
                    request.getRequestId(), statusCode, statusDesc, historyOneYear);

            /**
             * TH3: historyOneYear = null
             * → Tra CBC thất bại / không có thông tin → End luồng, yêu cầu KH thực hiện lại
             */

            if (Utility.isNull(historyOneYear)) {
                log.error("[DoCheckCBCSalaryAdvance] TH3: historyOneYear is null - CBC has no data - requestId:{}",
                        request.getRequestId());
                result = new SimpleResult(
                        "CBC request failed or timeout. Please contact MBCambodia for support.",
                        false, ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return false;
            }

            String history = historyOneYear.toLowerCase().trim();

            /**
             * TH2: historyOneYear = "normal" | "closed" | "reject" | "no information"
             * → KH không có nợ xấu → PASS, tiếp tục sang tính limit
             */
            if ("normal".equals(history) || "closed".equals(history)
                    || "reject".equals(history) || "no information".equals(history)) {
                log.info("[DoCheckCBCSalaryAdvance] TH2: PASS - historyOneYear={} - requestId:{}",
                        historyOneYear, request.getRequestId());
                // result stays OK → chain continues
            } else {
                /**
                 * TH1: historyOneYear != normal/closed/reject/no information
                 * → KH có nhóm nợ quá hạn trong 12 tháng → BLOCK
                 */
                log.error("[DoCheckCBCSalaryAdvance] TH1: BLOCK - historyOneYear={} - requestId:{}",
                        historyOneYear, request.getRequestId());
                result = new SimpleResult(
                        "We are unable to process your request at this time. Please contact MBCambodia for support.",
                        false, ResponseCode.TRANSACTION_FAIL.getCode());
            }

        } catch (Exception e) {
            log.error("[DoCheckCBCSalaryAdvance] Exception - requestId:{}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }
}
