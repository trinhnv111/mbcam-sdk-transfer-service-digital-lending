package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.api.CallAMLService;
import com.mbc.common.api.models.aml.CustAMLInfo;
import com.mbc.common.api.models.aml.CustAmlInput;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComSysParam;
import com.mbc.common.entity.Cust;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.repository.ComSysParamRepo;
import com.mbc.common.repository.CustRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.SystemParameterConstants;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustomerInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Command: Check AML — logic giống DoCustomerCheckAML
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoChecAMLSalaryAdvance implements Command {

    private final CallAMLService callAMLService;
    private final ComSysParamRepo comSysParamRepo;
    private final CustRepo custRepo;
    // Bổ sung ObjectMapper để serialize dữ liệu ra chuỗi JSON khi ghi log
    private final ObjectMapper objectMapper;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        EmCustomerInfo emCustInfo = (EmCustomerInfo) processContext.get("emCustomerInfo");

        try {
            log.info("[SA INIT - CHECK AML] Start - requestId:{}", request.getRequestId());

            ComSysParam comSysParam = comSysParamRepo.findByCd(SystemParameterConstants.ONBOARDING_CHECK_AML);
            if (Boolean.valueOf(comSysParam.getValue())) {

                // Build AML input — tên = familyName + firstName
                String customerName = emCustInfo.getFamilyName() + " " + emCustInfo.getFirstName();

                CustAmlInput amlInput = CustAmlInput.builder()
                        .customerName(customerName)
                        .customerType("INDIVIDUAL")
                        .dateOfBirth(emCustInfo.getDateOfBirth())
                        .documentNumber(emCustInfo.getIdNumber())
                        .gender(mapGender(emCustInfo.getGender()))   // "M"→"MALE", "F"→"FEMALE"
                        .nationality(emCustInfo.getNationality())
                        .requestedChannel(Constant.CHANNEL_MOBILE)
                        .requestedBusiness("CUSTOMER_ONBOARD")
                        .requestedUser(null)
                        .build();

                // GHI LOG REQUEST GỬI ĐI
                try {
                    log.info("[SA INIT - CHECK AML] Request AML API - requestId:{}, body:{}",
                            request.getRequestId(), objectMapper.writeValueAsString(amlInput));
                } catch (Exception ex) {
                    log.info("[SA INIT - CHECK AML] Request AML API - requestId:{}, body:{}",
                            request.getRequestId(), amlInput);
                }

                ExecuteT24Output<CustAMLInfo> output = callAMLService.customerCheckAML(
                        amlInput, null, request.getRequestId());

                // GHI LOG RESPONSE NHẬN VỀ
                try {
                    log.info("[SA INIT - CHECK AML] Response AML API - requestId:{}, output:{}",
                            request.getRequestId(), objectMapper.writeValueAsString(output));
                } catch (Exception ex) {
                    log.info("[SA INIT - CHECK AML] Response AML API - requestId:{}, output:{}",
                            request.getRequestId(), output);
                }

                if (output != null) {
                    if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                        String amlResult = output.getData().getResult();
                        request.setStatusCheckAML(amlResult);

                        // Save AML result vào bảng CUST
                        Cust cust = custRepo.findByIdTypNo(emCustInfo.getIdNumber());
                        if (Objects.nonNull(cust)) {
                            cust.setAml(amlResult);
                            custRepo.saveAndFlush(cust);
                        }

                        // PHÂN LOẠI CÁC TRƯỜNG HỢP (TH) CỦA AML RESULT
                        if ("TRUE_HIT".equals(amlResult)) {
                            // TH1: TRUE_HIT => Dừng luồng. Hiển thị thông báo vi phạm
                            String errorCode = ResponseCode.SA_CREDIT_REJECTED.getCode(); // Tùy chỉnh code lỗi nếu cần thiết
                            String errorMessage = String.format("We are unable to process your request at this time. Please contact MBCambodia for support. (%s)", errorCode);

                            result = new SimpleResult(errorMessage, false, errorCode);
                            processContext.setRequest(request);
                            processContext.setResult(result);
                            return !result.isOk(); // return true -> ngắt chain
                        } else if ("PENDING_HIT".equals(amlResult) || "NO_HIT".equals(amlResult)) {
                            // TH2 & TH3: PENDING_HIT hoặc NO_HIT => Đi tiếp luồng
                            log.info("[SA INIT - CHECK AML] Result is {}, process to next step - requestId:{}", amlResult, request.getRequestId());
                        } else {
                            // Trường hợp có trạng thái rác khác không xác định
                            log.warn("[SA INIT - CHECK AML] Result is Unrecognized ({}), process to next step - requestId:{}", amlResult, request.getRequestId());
                        }

                    } else {
                        // FAIL
                        request.setStatusCheckAML("CHECK_AML_FAIL");
                        result = new SimpleResult(ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getDesc(),
                                false, ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getCode());
                        processContext.setRequest(request);
                        processContext.setResult(result);
                        return !result.isOk();
                    }
                } else {
                    // TIMEOUT
                    request.setStatusCheckAML("CHECK_AML_TIMEOUT");
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(),
                            false, ResponseCode.REQUEST_TIMEOUT.getCode());
                    processContext.setRequest(request);
                    processContext.setResult(result);
                    return !result.isOk();
                }
            }

        } catch (Exception e) {
            request.setStatusCheckAML("CHECK_AML_FAIL");
            log.error("[SA INIT - CHECK AML] Exception - requestId:{}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getDesc(),
                    false, ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getCode());
        }

        processContext.setRequest(request);
        processContext.setResult(result);
        return !result.isOk();
    }

    /**
     * eMoney returns "M"/"F", AML API requires "MALE"/"FEMALE"
     */
    private String mapGender(String g) {
        if ("M".equalsIgnoreCase(g)) return "MALE";
        if ("F".equalsIgnoreCase(g)) return "FEMALE";
        return g;
    }
}