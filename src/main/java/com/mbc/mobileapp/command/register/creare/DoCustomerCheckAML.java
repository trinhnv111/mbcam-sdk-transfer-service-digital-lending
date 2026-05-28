package com.mbc.mobileapp.command.register.creare;

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
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.DateUtil;
import com.mbc.common.util.SystemParameterConstants;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DoCustomerCheckAML implements Command {

    @Autowired
    private CallAMLService callAMLService;

    @Autowired
    private CustRepo custRepo;

    @Autowired
    private ComSysParamRepo comSysParamRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        RegisterCustInfo openCustomerInfo = request.getRegisterCustInfo();

        try {
            ComSysParam comSysParam = comSysParamRepo.findByCd(SystemParameterConstants.ONBOARDING_CHECK_AML);
            if(Boolean.valueOf(comSysParam.getValue())){
                CustAmlInput custAmlInput = CustAmlInput.builder()
                        .customerName(openCustomerInfo.getCustName())
                        .customerType("INDIVIDUAL")
                        .dateOfBirth(DateFormatUtils.format(openCustomerInfo.getDob(), DateUtil.DATE_WITH_DASH_REVERSE))
                        .documentNumber(openCustomerInfo.getIdCardNumber())
                        .gender(openCustomerInfo.getGender())
                        .nationality(openCustomerInfo.getNationalId())
                        .requestedBusiness("CUSTOMER_ONBOARD")
                        .requestedChannel(Constant.CHANNEL_MOBILE)
                        .requestedUser(null)
                        .build();
                ExecuteT24Output<CustAMLInfo> output = callAMLService.customerCheckAML(custAmlInput, null, request.getRequestId());
                if(output != null){
                    if(Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())){
                        request.setStatusCheckAML(output.getData().getResult());
                        Cust cust = custRepo.findByIdTypNo(openCustomerInfo.getIdCardNumber());
                        if(Objects.nonNull(cust)){
                            cust.setAml(output.getData().getResult());
                            cust.setNationalId(openCustomerInfo.getNationalId());
                            custRepo.saveAndFlush(cust);
                        }


                        if ("TRUE_HIT".equals(output.getData().getResult())){
                            result = new SimpleResult(ResponseCode.CUSTOMER_INFO_CHECK_AML_TRUE_HIT.getDesc(), false,
                                    ResponseCode.CUSTOMER_INFO_CHECK_AML_TRUE_HIT.getCode());
                            context.setRequest(request);
                            context.setResult(result);
                            return !result.isOk();
                        }
                    }else{
                        request.setStatusCheckAML("CHECK_AML_FAIL");
                        result = new SimpleResult(ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getDesc(), false,
                                ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getCode());
                        context.setRequest(request);
                        context.setResult(result);
                        return !result.isOk();
                    }
                }else{
                    request.setStatusCheckAML("CHECK_AML_TIMEOUT");
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());
                    context.setRequest(request);
                    context.setResult(result);
                    return !result.isOk();
                }
            }


        }catch (Exception e){
            request.setStatusCheckAML("CHECK_AML_FAIL");
            AppLog.error("[Exception Check AML] requestId: " + request.getRequestId() + " desc: ", e);
            result = new SimpleResult(ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getDesc(), false,
                    ResponseCode.CUSTOMER_INFO_CHECK_AML_FAIL.getCode());
        }
        context.setRequest(request);
        context.setResult(result);
        return !result.isOk();
    }
}
