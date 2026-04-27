package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Command: PD Check (Pre-Disbursement)
 *   1. Load bản ghi tạm từ COM_LOAN_DISBUR_LMT bằng tempRecordId
 *   2. Verify bản ghi tồn tại, đúng customer (hostCifId), đúng step (CHECK_CUST)
 *   3. Put bản ghi tạm vào context cho command sau (DoCheckCbc, ...) dùng.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoCheckPd implements Command {

    private static final String STEP_CHECK_CUST = "CHECK_CUST";

    private final ComTransDtlLmtRepository comTransDtlLmtRepo;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = processContext.getCustomer();

        try {
            SalaryAdvanceCreateRequest createRequest = request.getSalaryAdvanceCreateRequest();
            String tempRecordId = createRequest.getTempRecordId();

            log.info("[SA CREATE - CHECK PD] Start - requestId:{}, tempRecordId:{}",
                    request.getRequestId(), tempRecordId);

            ComTransDtlLmt tempRecord = comTransDtlLmtRepo.findById(tempRecordId).orElse(null);
            if (Objects.isNull(tempRecord)) {
                log.error("[SA CREATE - CHECK PD] Temp record not found - id:{}", tempRecordId);
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        "Temp record not found");
                processContext.setResult(result);
                return true;
            }

            if (!custInfo.getHostCifId().equals(tempRecord.getHostCifId())) {
                log.error("[SA CREATE - CHECK PD] HostCifId mismatch - session:{}, record:{}",
                        custInfo.getHostCifId(), tempRecord.getHostCifId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        "Customer mismatch");
                processContext.setResult(result);
                return true;
            }

            if (!STEP_CHECK_CUST.equals(tempRecord.getStep())) {
                log.error("[SA CREATE - CHECK PD] Invalid step - expected:{}, actual:{}",
                        STEP_CHECK_CUST, tempRecord.getStep());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        "Invalid temp record step");
                processContext.setResult(result);
                return true;
            }

            processContext.put("tempRecord", tempRecord);

            log.info("[SA CREATE - CHECK PD] Passed - requestId:{}", request.getRequestId());

        } catch (Exception e) {
            log.error("[SA CREATE - CHECK PD] Exception - requestId:{}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    ResponseCode.TRANSACTION_FAIL.getDesc());
        }

        processContext.setResult(result);
        return !result.isOk();
    }
}
