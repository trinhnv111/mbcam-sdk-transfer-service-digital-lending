package com.mbc.mobileapp.process;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mbc.common.api.CallServiceEkyc;
import com.mbc.common.api.models.ekyc.UserBindingPostCheckRequest;
import com.mbc.common.api.models.ekyc.UserBindingPostCheckResponse;
import com.mbc.common.api.models.ekyc.UserData;
import com.mbc.common.bean.Request;
import com.mbc.common.constants.OnboardingStep;
import com.mbc.common.entity.ComAuthEkyc;
import com.mbc.common.entity.ComLogOnboarding;
import com.mbc.common.entity.Cust;
import com.mbc.common.repository.ComAuthEkycRepo;
import com.mbc.common.repository.ComLogOnboardingRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;

@Component
@Scope("prototype")
public class BindingPostCheckProcess extends Thread {
    
    @Autowired
    private ComAuthEkycRepo comAuthEkycRepo;
    
    @Autowired
    private CallServiceEkyc callServiceEkyc;
    
    @Autowired
    private ComLogOnboardingRepo comLogOnboardingRepo;
    
    private Request request = null; 
    private Cust cust = null;
    private String bioId = null;
    
    public void setData(Cust cust, String bioId, Request request) {               
        this.request = request;
        this.cust = cust;
        this.bioId = bioId;
      }
    
    @Override
    public void run() {
        try {
            bindingEkyc(cust, bioId, request);
        }
        catch (Exception e) {
            AppLog.error("ERROR", e);
//            result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
        }
    }
    
    private void bindingEkyc(Cust cust, String bioId, Request request) throws Exception {

        ComAuthEkyc comAuthEkyc = comAuthEkycRepo.findByCustIdAndBioId(cust.getId(), bioId);
//        comAuthEkyc = mapper.convertValue(authenEkyc, ComAuthEkyc.class);
//        comAuthEkyc.setCustId(cust.getId());
//        comAuthEkyc.setCreatedBy(Constant.CREATED_BY_SYSTEM);
//        if (!Utility.isNull(cust.getHostCifId())) {
//            comAuthEkyc.setHashUserBank(Utility.sha256(cust.getHostCifId()));
//        }
//        comAuthEkycRepo.saveAndFlush(comAuthEkyc);

        UserData userData = new UserData();
        if (cust.getDob() != null) {
            userData.setBirthday(DateFormatUtils.format(cust.getDob(), "dd/MM/yyyy"));
        }
        if (!Utility.isNull(cust.getGenderCd())) {
            userData.setGender(cust.getGenderCd().toUpperCase());
        }
        if (!Utility.isNull(cust.getIdTypNo())) {
            userData.setIdNumber(cust.getIdTypNo());
        }
        if (cust.getIdTypDt() != null) {
            userData.setIssueDate(DateFormatUtils.format(cust.getIdTypDt(), "dd/MM/yyyy"));
        }
        if (!Utility.isNull(cust.getIdTypPlace())) {
            userData.setIssuePlace(cust.getIdTypPlace());
        }
        userData.setName(cust.getNm());
        userData.setNationality(null);
        userData.setRecentLocation(cust.getAddr1());
        userData.setTypeCard(cust.getIdTypType());
        userData.setValidDate(null);

        UserBindingPostCheckRequest params = new UserBindingPostCheckRequest();
        params.setBioId(comAuthEkyc.getBioId());
        params.setCif(cust.getHostCifId());
        params.setDeviceId(comAuthEkyc.getDeviceId());
        params.setEkycType(comAuthEkyc.getEkycType());
        params.setHashBankID(comAuthEkyc.getHashUserBank());
        params.setLevelUser(comAuthEkyc.getBioLevel());
        params.setNeedBinding(true);
        params.setSessionId(comAuthEkyc.getSessionId());
        params.setTimestamp(String.valueOf(new Date().getTime()));
        params.setUserData(userData);
        
        Date startDate = new Date();
        
        UserBindingPostCheckResponse response = callServiceEkyc.userBindingPostCheck(params, cust.getId(), request.getRequestId(),
                Utility.getUUID());
        BigDecimal timeProcess = new BigDecimal(Calendar.getInstance().getTimeInMillis() - startDate.getTime());
            
        if (Utility.isNull(response.getErrorCode())) {
            comAuthEkyc.setBindingPCheckCode(response.getCode());
            comAuthEkyc.setBindingPCheckDesc(response.getMessage());
            comAuthEkycRepo.saveAndFlush(comAuthEkyc);
            logBinding(response.getCode(), response.getMessage(), startDate, timeProcess);

        } else {
            comAuthEkyc.setBindingPCheckCode(response.getErrorCode());
            comAuthEkyc.setBindingPCheckDesc("Error - " + response.getErrorCode());
            comAuthEkycRepo.saveAndFlush(comAuthEkyc);
            logBinding(response.getErrorCode(), response.getErrorCode(), startDate, timeProcess);

        }
        
        

    }
    
    private void logBinding(String code, String desc, Date requestTime, BigDecimal timeProcess) {
        
        ComLogOnboarding comLogOnboarding = ComLogOnboarding.builder()
            .deviceId(request.getDeviceIdCommon())
            .routeKey(request.getRouteKey())
            .step(OnboardingStep.STEP_BINDING)
            .responseCode(code)
            .responseDesc(desc)
            .requestTime(requestTime)
            .timeProcess(timeProcess)
            .requestId(request.getRequestId())
            .channel(Constant.CHANNEL_MOBILE)
            .build();
        comLogOnboardingRepo.saveAndFlush(comLogOnboarding);
        
//        ComLogOnboarding comLogOnboarding = comLogOnboardingRepo.findByDeviceIdAndRouteKey(request.getDeviceIdCommon(), request.getRouteKey());
//        if(Objects.isNull(comLogOnboarding)) {
//            comLogOnboarding = ComLogOnboarding.builder()
//                .binding(binding)               
//                .build();
//            comLogOnboardingRepo.saveAndFlush(comLogOnboarding);
//                
//        }else {
//            comLogOnboarding.setBinding(binding);           
//            comLogOnboardingRepo.saveAndFlush(comLogOnboarding);
//        }
    }

}
