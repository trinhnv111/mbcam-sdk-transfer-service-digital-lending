package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.ApiCbcPortal;
import com.mbc.mobileapp.api.model.cbc.input.GetCbcDataRequest;
import com.mbc.mobileapp.api.model.cbc.output.CbcDataItem;
import com.mbc.mobileapp.api.model.cbc.output.GetCbcDataResponse;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Command: CBC Check — gọi API /integrate/v1/get-cbc-data theo spec CBC v2.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoCheckCbc implements Command {

    private static final String APP_CODE   = "MOBILEAPP";
    private static final String STATUS_OK  = "000";

    private final ApiCbcPortal apiCbcPortal;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = processContext.getCustomer();
        String requestId = request.getRequestId();

        // 1. Lấy tempRecord từ context (do DoCheckPd put)
        ComTransDtlLmt tempRecord = (ComTransDtlLmt) processContext.get("tempRecord");
        if (tempRecord == null) {
            log.error("[SA CREATE - CHECK CBC] Missing tempRecord in context - requestId:{}", requestId);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false, "Missing tempRecord");
            processContext.setResult(result);
            return true;
        }

        // 2. Lấy idNumber
        String idNumber = Utility.isNull(tempRecord.getNationalId())
                ? custInfo.getIdTypNo()
                : tempRecord.getNationalId();

        if (Utility.isNull(idNumber)) {
            log.error("[SA CREATE - CHECK CBC] idNumber is blank - requestId:{}", requestId);
            result = new SimpleResult(ResponseCode.INVALID_INPUT.getCode(), false, "Missing idNumber");
            processContext.setResult(result);
            return true;
        }

        log.info("[SA CREATE - CHECK CBC] Start - requestId:{}, tempId:{}, idNumber:{}",
                requestId, tempRecord.getId(), idNumber);

        // 3. Build request body theo spec
        String requestBy = custInfo.getUserId() != null ? custInfo.getUserId() : custInfo.getId();
        GetCbcDataRequest body = GetCbcDataRequest.builder()
                .requestBy(requestBy)
                .appCode(APP_CODE)
                .idNumber(Collections.singletonList(idNumber))
                .build();

        // 4. Gọi API CBC portal
        ResponseEntity<GetCbcDataResponse> responseEntity =
                apiCbcPortal.getCbcData(body, requestBy, requestId);

        if (responseEntity == null) {
            log.error("[SA CREATE - CHECK CBC] HTTP TIMEOUT/CONN - requestId:{}", requestId);
            result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                    ResponseCode.REQUEST_TIMEOUT.getCode());
            processContext.setResult(result);
            return true;
        }

        // 5. Kiểm tra HTTP status
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            log.error("[SA CREATE - CHECK CBC] HTTP error:{} - requestId:{}",
                    responseEntity.getStatusCode().value(), requestId);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    "CBC portal HTTP " + responseEntity.getStatusCode().value());
            processContext.setResult(result);
            return true;
        }

        GetCbcDataResponse resp = responseEntity.getBody();
        if (resp == null || resp.getData() == null || resp.getData().isEmpty()) {
            log.error("[SA CREATE - CHECK CBC] Response body empty - requestId:{}", requestId);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    "CBC response empty");
            processContext.setResult(result);
            return true;
        }

        // 6. Lấy item tương ứng idNumber
        CbcDataItem item = resp.getData().get(0);
        String statusCode = item.getStatusCode();

        log.info("[SA CREATE - CHECK CBC] statusCode:{}, statusDesc:{}, reportId:{}, enquiryRef:{} - requestId:{}",
                statusCode, item.getStatusDesc(), item.getReportId(), item.getENQUIRY_REFERENCE(), requestId);

        // 7. Xử lý statusCode theo spec
        switch (statusCode != null ? statusCode : "") {
            case STATUS_OK:
                // Effective — pass
                processContext.put("cbcDataItem",      item);
                processContext.put("cbcReportId",      item.getReportId());
                processContext.put("cbcEnquiryRef",    item.getENQUIRY_REFERENCE());
                processContext.put("cbcCurrentStatus", item.getCurrentStatus());
                log.info("[SA CREATE - CHECK CBC] Passed (000 Effective) - requestId:{}, reportId:{}, currentStatus:{}",
                        requestId, item.getReportId(), item.getCurrentStatus());
                break;

            case "011":
                log.warn("[SA CREATE - CHECK CBC] statusCode 011 - CBC report is expired - requestId:{}", requestId);
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        "CBC report is expired");
                break;

            case "012":
                log.warn("[SA CREATE - CHECK CBC] statusCode 012 - Got error from CBC side - requestId:{}", requestId);
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        "Got error from CBC side");
                break;

            case "013":
                log.warn("[SA CREATE - CHECK CBC] statusCode 013 - CBC report is waiting approval - requestId:{}", requestId);
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        "CBC report is waiting approval");
                break;

            case "014":
                log.warn("[SA CREATE - CHECK CBC] statusCode 014 - CBC report is declined - requestId:{}", requestId);
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        "CBC report is declined");
                break;

            case "015":
                log.warn("[SA CREATE - CHECK CBC] statusCode 015 - No CBC report - requestId:{}", requestId);
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        "No CBC report");
                break;

            default:
                log.error("[SA CREATE - CHECK CBC] Unknown statusCode:{} - requestId:{}", statusCode, requestId);
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        "CBC unknown status: " + statusCode);
                break;
        }

        processContext.setResult(result);
        return !result.isOk();
    }
}
