package com.mbc.mobileapp.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.api.ApiLoyalty;
import com.mbc.common.api.models.loyalty.balance.LoyaltyBalanceOutput;
import com.mbc.common.api.models.loyalty.register.LoyaltyRegisterInput;
import com.mbc.common.api.models.loyalty.register.LoyaltyRegisterOutput;
import com.mbc.common.entity.ComLoyaltyPoints;
import com.mbc.common.entity.ComRegisterLoyaltyLog;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComLoyaltyPointsRepo;
import com.mbc.common.repository.ComRegisterLoyaltyLogRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;

@Component
@Scope("prototype")
public class RegisterLoyaltyProcess extends Thread {
    
    private CustInfo custInfo;
    
    private String requestId;
    
    @Autowired
    private ApiLoyalty apiLoyalty;

    @Autowired
    private ComRegisterLoyaltyLogRepo comRegisterLoyaltyLogRepo;

    @Autowired
    private ComLoyaltyPointsRepo comLoyaltyPointsRepo;
    
    public void setData(CustInfo custInfo, String requestId) {               
        this.requestId = requestId;
        this.custInfo = custInfo;       
    }
    
    @Override
    public void run() {
        try {
         // Dang ky loyalty
            LoyaltyRegisterInput input = new LoyaltyRegisterInput();
            input.setCustomerCode(custInfo.getHostCifId());
            input.setDisabled(Constant.NO);
            input.setEmail(custInfo.getCorrespondentEmail());
            input.setFirstName(custInfo.getNm());
            input.setFullName(custInfo.getNm());
            input.setLastName(custInfo.getNm());
            input.setMiddleName(custInfo.getNm());
            input.setOrganizationId(custInfo.getOrgUnitCd());
            input.setPhone(custInfo.getPhoneNo());
            
            LoyaltyRegisterOutput output = apiLoyalty.register(input, custInfo.getId(), requestId);
            ObjectMapper OBJECT_MAPPER =
                new ObjectMapper().configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
            ComRegisterLoyaltyLog registerLoyalty = new ComRegisterLoyaltyLog();
            registerLoyalty.setRequest(OBJECT_MAPPER.writeValueAsString(input));
            registerLoyalty.setCreatedBy(custInfo.getUserId());
            registerLoyalty.setCustId(custInfo.getId());
            if (output != null && !Utility.isNull(output.getCustomerId())) {
                registerLoyalty.setResponse(OBJECT_MAPPER.writeValueAsString(output));
                registerLoyalty.setStatus(Constant.STATUS_SUCCESS);

                LoyaltyBalanceOutput balanceOutput = apiLoyalty.getBalance(custInfo, requestId);
                if (Constant.STATUS_SUCCESS.equals(balanceOutput.getStatus())) {
                    ComLoyaltyPoints comLoyaltyPoints = new ComLoyaltyPoints();
                    comLoyaltyPoints.setCustId(custInfo.getId());
                    comLoyaltyPoints.setTotalPoint(Integer.valueOf(balanceOutput.getResult()));
                    comLoyaltyPoints.setCurentPoint(Integer.valueOf(balanceOutput.getResult()));
                    comLoyaltyPoints.setCreatedBy(Constant.CHANNEL_EMB);
                    comLoyaltyPointsRepo.saveAndFlush(comLoyaltyPoints);
                }

            }
            else {
                registerLoyalty.setResponse(OBJECT_MAPPER.writeValueAsString(output));
                registerLoyalty.setStatus(Constant.STATUS_FAILED);
            }
            comRegisterLoyaltyLogRepo.saveAndFlush(registerLoyalty);
        }
        catch (Exception e) {
            AppLog.error("ERROR", e);
        }
    }

}
