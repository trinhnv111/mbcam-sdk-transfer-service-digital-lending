package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Command: Lưu kết quả tính limit vào bản ghi tạm, update status → COM (completed)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoSaveSalaryAdvanceRecord implements Command {

    private static final String STEP_CREATE = "CREATE";
    private static final String STATUS_COMPLETED = "COM";

    private final ComTransDtlLmtRepository comTransDtlLmtRepo;
    private final ComTransRepo comTransRepo;
    private final ComTransProcessRepo comTransProcessRepo;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = processContext.getCustomer();

        try {
            ComTransDtlLmt tempRecord = (ComTransDtlLmt) processContext.get("tempRecord");
            BigDecimal calculatedLimit = (BigDecimal) processContext.get("calculatedLimit");
            String limitCurrency = (String) processContext.get("limitCurrency");

            log.info("[SA CREATE - SAVE RECORD] Start - requestId:{}, tempId:{}, limit:{}",
                    request.getRequestId(), tempRecord.getId(), calculatedLimit);

            // ===== 1. Update bản ghi tạm: gán limit, cập nhật status → COM =====
            tempRecord.setLoanLimit(calculatedLimit);
            tempRecord.setApproveLimit(calculatedLimit);
            tempRecord.setUsedLimit(BigDecimal.ZERO);
            tempRecord.setCurrency(limitCurrency);
            tempRecord.setStep(STEP_CREATE);
            tempRecord.setStatus(STATUS_COMPLETED);
            tempRecord.setCreatedDateLimit(new Date());
            // Đặt thời hạn limit (ví dụ: 30 ngày)
            tempRecord.setStartDate(new Date());

            comTransDtlLmtRepo.saveAndFlush(tempRecord);

            // ===== 2. Update ComTrans status → COM =====
            ComTrans comTrans = comTransRepo.findById(tempRecord.getId()).orElse(null);
            if (comTrans != null) {
                comTrans.setStatus(STATUS_COMPLETED);
                comTransRepo.saveAndFlush(comTrans);
            }

            // ===== 3. Update ComTransProcess status → COM =====
            // ComTransProcess tìm theo transId = tempRecord.getId()

            // ===== 4. Build response data và put vào context =====
            SalaryAdvanceCreateResponse responseData = new SalaryAdvanceCreateResponse();
            responseData.setCustId(custInfo.getId());
            responseData.setHostCifId(custInfo.getHostCifId());
            responseData.setCustomerName(tempRecord.getFullName());
            responseData.setNationalId(tempRecord.getNationalId());
            responseData.setPhoneNumber(custInfo.getPhoneNo());
            responseData.setAddressProvince(tempRecord.getAddressProvince());
            responseData.setAddressDistrict(tempRecord.getAddressDistrict());
            responseData.setAddressWard(tempRecord.getAddressWard());
            responseData.setMaritalStatus(tempRecord.getMaritalStatus());
            responseData.setLimit(calculatedLimit);
            responseData.setCurrency(limitCurrency);

            processContext.put("createResponse", responseData);

            log.info("[SA CREATE - SAVE RECORD] Done - requestId:{}, limit:{} {}",
                    request.getRequestId(), calculatedLimit, limitCurrency);

        } catch (Exception e) {
            log.error("[SA CREATE - SAVE RECORD] Exception - requestId:{}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    ResponseCode.TRANSACTION_FAIL.getDesc());
        }

        processContext.setResult(result);
        return !result.isOk();
    }
}
