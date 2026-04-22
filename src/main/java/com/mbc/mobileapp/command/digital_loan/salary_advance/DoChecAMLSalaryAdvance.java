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
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoOutput;
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
public class DoChecAMLSalaryAdvance implements Command {

    private final CallAMLService callAMLService;
    private final ComSysParamRepo comSysParamRepo;
    private final CustRepo custRepo;

//    3 check aml
    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest commonServiceRequest = (CommonServiceRequest) processContext.getRequest();
        // lấy thông tin từ EMoney
        EmCustInfoOutput emCustInfoOutput =(EmCustInfoOutput) processContext.get("emCustInfoOutput");

        try {
            // check toge comSysParamRepo
            log.info("[SA INIT ] - CHECK AML - requestId :{}", commonServiceRequest.getRequestId());
            ComSysParam comSysParam = comSysParamRepo.findByCd(SystemParameterConstants.ONBOARDING_CHECK_AML);
            if(Boolean.valueOf(comSysParam.getValue())){
                // build aml từ EMoney
                CustAmlInput amlInput = CustAmlInput.builder()
                        .customerName(emCustInfoOutput.getEnglishName())
                        .customerType("INDIVIDUAL")
                        .dateOfBirth(emCustInfoOutput.getDateOfBirth())
                        .documentNumber(emCustInfoOutput.getIdNumber()) // Số giấy tờ định danh
                        .gender(emCustInfoOutput.getGender())
                        .nationality(emCustInfoOutput.getNationalId())
                        .requestedChannel(Constant.CHANNEL_MOBILE)
                        .requestedBusiness("CUSTOMER_ONBOARD")
                        .requestedUser(null)
                        .build();
                ExecuteT24Output<CustAMLInfo> output = callAMLService.customerCheckAML(amlInput,null,commonServiceRequest.getRequestId());
                if(output != null){
                    if(Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())){
                        commonServiceRequest.setStatusCheckAML(output.getData().getResult());
                        Cust cust = custRepo.findByIdTypNo(emCustInfoOutput.getIdNumber());
                        if(Objects.nonNull(cust)){
                            cust.setAml(output.getData().getResult());
                            cust.setNationalId(emCustInfoOutput.getNationalId());
                            custRepo.saveAndFlush(cust);
                        }

                        if("TRUE_HIT".equals(output.getData().getResult())){
                            result  = new SimpleResult(ResponseCode.CUSTOMER_INFO_CHECK_AML_TRUE_HIT.getDesc(),false,ResponseCode.CUSTOMER_INFO_CHECK_AML_TRUE_HIT.getCode());
                            processContext.setRequest(commonServiceRequest);
                            processContext.setResult(result);

                            return  !result.isOk();
                        }
                    }
                    else {
                        commonServiceRequest.setStatusCheckAML("CHECK_AML_FAIL");
                        result  = new SimpleResult(ResponseCode.CUSTOMER_INFO_CHECK_AML_TRUE_HIT.getDesc(),false,ResponseCode.CUSTOMER_INFO_CHECK_AML_TRUE_HIT.getCode());
                        processContext.setRequest(commonServiceRequest);
                        processContext.setResult(result);

                        return !result.isOk();
                    }
                }
                else {
                    commonServiceRequest.setStatusCheckAML("CHECK_AML_TIMEOUT");
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false, ResponseCode.REQUEST_TIMEOUT.getCode());
                    processContext.setRequest(commonServiceRequest);
                    processContext.setResult(result);

                    return !result.isOk();
                }
            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        processContext.setResult(result);
        return !result.isOk();
    }
}
