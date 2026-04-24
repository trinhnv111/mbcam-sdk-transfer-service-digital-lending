package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.api.CallAMLService;
import com.mbc.common.api.models.aml.CustAMLInfo;
import com.mbc.common.api.models.aml.CustAmlInput;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComSysParam;
import com.mbc.common.entity.Cust;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
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
 * Dùng EmCustomerInfo (Nhóm 1) để build AML input
 * Tên KH = familyName + " " + firstName (vì englishName là Boolean)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoChecAMLSalaryAdvance implements Command {

    private final CallAMLService callAMLService;
    private final ComSysParamRepo comSysParamRepo;
    private final CustRepo custRepo;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        EmCustomerInfo emCustInfo = (EmCustomerInfo) processContext.get("emCustomerInfo");

        try {
            log.info("[SA INIT - CHECK AML] Start - requestId:{}", request.getRequestId());

            // Check toggle ComSysParam (giống DoCustomerCheckAML)
            ComSysParam comSysParam = comSysParamRepo.findByCd(SystemParameterConstants.ONBOARDING_CHECK_AML);
            if (Boolean.valueOf(comSysParam.getValue())) {

                // Build AML input — tên = familyName + firstName
                String customerName = emCustInfo.getFamilyName() + " " + emCustInfo.getFirstName();

                CustAmlInput amlInput = CustAmlInput.builder()
                        .customerName(customerName)
                        .customerType("INDIVIDUAL")
                        .dateOfBirth(emCustInfo.getDateOfBirth())
                        .documentNumber(emCustInfo.getIdNumber())
                        .gender(emCustInfo.getGender())           // MALE / FEMALE / OTHER
                        .nationality(emCustInfo.getNationality())  // KH / VN
                        .requestedChannel(Constant.CHANNEL_MOBILE)
                        .requestedBusiness("CUSTOMER_ONBOARD")
                        .requestedUser(null)
                        .build();

                ExecuteT24Output<CustAMLInfo> output = callAMLService.customerCheckAML(
                        amlInput, null, request.getRequestId());

                if (output != null) {
                    if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                        request.setStatusCheckAML(output.getData().getResult());

                        // Save AML result vào bảng CUST
                        Cust cust = custRepo.findByIdTypNo(emCustInfo.getIdNumber());
                        if (Objects.nonNull(cust)) {
                            cust.setAml(output.getData().getResult());
                            custRepo.saveAndFlush(cust);
                        }

                        // TRUE_HIT → từ chối
                        if ("TRUE_HIT".equals(output.getData().getResult())) {
                            result = new SimpleResult(ResponseCode.CUSTOMER_INFO_CHECK_AML_TRUE_HIT.getDesc(),
                                    false, ResponseCode.CUSTOMER_INFO_CHECK_AML_TRUE_HIT.getCode());
                            processContext.setRequest(request);
                            processContext.setResult(result);
                            return !result.isOk();
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

//                return true;
            }

        } catch (Exception e) {
            // Fix: không throw RuntimeException, return error giống DoCustomerCheckAML
            request.setStatusCheckAML("CHECK_AML_FAIL");
            log.error("[SA INIT - CHECK AML] Exception - requestId:{}", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getDesc(),
                    false, ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getCode());
        }

        processContext.setRequest(request);
        processContext.setResult(result);
        return !result.isOk();
    }
}
