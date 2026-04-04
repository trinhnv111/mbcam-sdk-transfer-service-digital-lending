package com.mbc.mobileapp.command.register;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.constants.OnboardingStep;
import com.mbc.common.entity.ComLogOnboarding;
import com.mbc.common.entity.Cust;
import com.mbc.common.repository.ComLogOnboardingRepo;
import com.mbc.common.repository.CustRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

@Service
public class DoValidPhoneNo implements Command {

    private static final String PHONE_CAM_PATTERN = "^[0-9\\-\\+]{9,12}$"; //"^0[0-9]{8}$";

    @Autowired
    private CustRepo custRepo;

    @Autowired
    private ComLogOnboardingRepo comLogOnboardingRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

         if (!isPhoneNumber(request.getPhoneNo())) {
             result = new SimpleResult(MBCResponseCode.PHONE_NO_IS_INVALID.getDesc(), false,
                     MBCResponseCode.PHONE_NO_IS_INVALID.getCode());
             context.setResponse(response);
             context.setResult(result);
             return !result.isOk();
         }

        List<Cust> custList = custRepo.findByIsDeleteAndPhoneNoAndUserIdNotNull(Constant.NO, request.getPhoneNo());

        if (custList != null && !custList.isEmpty()) {
            result = new SimpleResult(MBCResponseCode.PHONE_NO_EXITSTED.getDesc(), false,
                MBCResponseCode.PHONE_NO_EXITSTED.getCode());
        }
        
        context.setResponse(response);
        context.setResult(result);
        logValidatePhoneNumber(request, result);
        return !result.isOk();
    }

    private boolean isPhoneNumber(String phoneNumber) {
        return !StringUtils.isBlank(phoneNumber) && phoneNumber.matches(PHONE_CAM_PATTERN);
    }

    private void logValidatePhoneNumber(CommonServiceRequest request, Validator.Result result){
        ComLogOnboarding comLogOnboarding = ComLogOnboarding.builder()
                .deviceId(request.getDeviceIdCommon())
                .routeKey(request.getRouteKey())
                .step(OnboardingStep.STEP_CHECK_PHONE_NUMBER)
                .responseCode(result.getResponseCode())
                .responseDesc(result.getMessage())
                .content(request.getPhoneNo())
                .phoneNumber(request.getPhoneNo())
                .timeProcess(new BigDecimal(Calendar.getInstance().getTimeInMillis() - request.getStartRequest().getTime()))
                .requestTime(request.getStartRequest())
                .requestId(request.getRequestId())
                .channel(request.getDigitalChannel())
                .build();
        comLogOnboardingRepo.saveAndFlush(comLogOnboarding);
    }

}
