package com.mbc.mobileapp.rest.register;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.constants.OnboardingStep;
import com.mbc.common.entity.ComLogOnboarding;
import com.mbc.common.repository.ComLogOnboardingRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;

@Slf4j
@Service
public class DoLogValidOtpPhoneNo implements Command {
    
    @Autowired
    private ComLogOnboardingRepo comLogOnboardingRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = context.getResult();

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();   
        try {
            
            ComLogOnboarding comLogOnboarding = ComLogOnboarding.builder()
                .deviceId(request.getDeviceIdCommon())
                .routeKey(request.getRouteKey())
                .step(OnboardingStep.STEP_CHECK_OTP)
                .responseCode(result.getResponseCode())
                .responseDesc(result.getMessage())
                .timeProcess(new BigDecimal(Calendar.getInstance().getTimeInMillis() - request.getStartRequest().getTime()))
                .requestTime(request.getStartRequest())
                .requestId(request.getRequestId())
                .channel(request.getDigitalChannel())
                .build();
            comLogOnboardingRepo.saveAndFlush(comLogOnboarding);
            
//            ComLogOnboarding comLogOnboarding = comLogOnboardingRepo.findByDeviceIdAndRouteKey(request.getDeviceIdCommon(), request.getRouteKey());
//            if(Objects.isNull(comLogOnboarding)) {
//                comLogOnboarding = ComLogOnboarding.builder()
//                    .deviceId(request.getDeviceIdCommon())
//                    .routeKey(request.getRouteKey())
//                    .phoneNo(request.getPhoneNo())
//                    .validPhoneNo(result.getResponseCode() + "-" + result.getMessage())
//                    .build();
//                comLogOnboardingRepo.saveAndFlush(comLogOnboarding);
//                    
//            }else {
//                comLogOnboarding.setDeviceId(request.getDeviceIdCommon());
//                comLogOnboarding.setRouteKey(request.getRouteKey());
//                comLogOnboarding.setPhoneNo(request.getPhoneNo());
//                comLogOnboarding.setValidPhoneNo(result.getResponseCode() + "-" + result.getMessage());
//                comLogOnboardingRepo.saveAndFlush(comLogOnboarding);
//            }
        }
        catch (Exception e) {
            AppLog.error("[Exception Log Valid Phone No] requestId: "+request.getRequestId()+" desc: {} ", e);
        }       
        return !SimpleResult.OK.isOk();
    }

}
