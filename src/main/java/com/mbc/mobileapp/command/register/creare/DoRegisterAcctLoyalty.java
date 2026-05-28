package com.mbc.mobileapp.command.register.creare;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.api.ApiLoyalty;
import com.mbc.common.api.models.loyalty.balance.LoyaltyBalanceOutput;
import com.mbc.common.api.models.loyalty.register.LoyaltyRegisterInput;
import com.mbc.common.api.models.loyalty.register.LoyaltyRegisterOutput;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.entity.ComCashbackCampaign;
import com.mbc.common.entity.ComLoyaltyPoints;
import com.mbc.common.entity.ComRegisterLoyaltyLog;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComCashbackCampaignRepo;
import com.mbc.common.repository.ComLoyaltyPointsRepo;
import com.mbc.common.repository.ComRegisterLoyaltyLogRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DoRegisterAcctLoyalty implements Command {
    
    @Autowired
    private ApiLoyalty apiLoyalty;

    @Autowired
    private ComRegisterLoyaltyLogRepo comRegisterLoyaltyLogRepo;

    @Autowired
    private ComLoyaltyPointsRepo comLoyaltyPointsRepo;
    
    @Autowired
    private ComCashbackCampaignRepo comCashbackCampaignRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        Validator.Result result = Validator.Result.OK;
//        OpenCustomerInfo openCustomerInfo = request.getOpenCustomerInfo();
        CustInfo custInfo = context.getCustomer();     
        try {
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
            
            LoyaltyRegisterOutput output = apiLoyalty.register(input, custInfo.getId(), request.getRequestId());
            ObjectMapper OBJECT_MAPPER =
                new ObjectMapper().configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
            ComRegisterLoyaltyLog registerLoyalty = new ComRegisterLoyaltyLog();
            registerLoyalty.setRequest(OBJECT_MAPPER.writeValueAsString(input));
            registerLoyalty.setCreatedBy(custInfo.getUserId());
            registerLoyalty.setCustId(custInfo.getId());
            if (output != null && !Utility.isNull(output.getCustomerId())) {
                registerLoyalty.setResponse(OBJECT_MAPPER.writeValueAsString(output));
                registerLoyalty.setStatus(Constant.STATUS_SUCCESS);
                context.putVar(Constant.KeyVar.REGISTER_LOYALTY, Constant.STATUS_SUCCESS);

                LoyaltyBalanceOutput balanceOutput = apiLoyalty.getBalance(custInfo, request.getRequestId());
                if (Constant.STATUS_SUCCESS.equals(balanceOutput.getStatus())) {
                    ComLoyaltyPoints comLoyaltyPoints = new ComLoyaltyPoints();
                    comLoyaltyPoints.setCustId(custInfo.getId());
                    comLoyaltyPoints.setTotalPoint(Integer.valueOf(balanceOutput.getResult()));
                    comLoyaltyPoints.setCurentPoint(Integer.valueOf(balanceOutput.getResult()));
                    comLoyaltyPoints.setCreatedBy("");
                    comLoyaltyPointsRepo.saveAndFlush(comLoyaltyPoints);
                }

            }
            else {
                registerLoyalty.setResponse(OBJECT_MAPPER.writeValueAsString(output));
                registerLoyalty.setStatus(Constant.STATUS_FAILED);
                
                ComCashbackCampaign comCashbackCampaign = ComCashbackCampaign.builder()
                    .custId(custInfo.getId())
                    .type(null)
                    .stage(Constant.CashbackCampaignState.ERR_REGISTER_LOYALTY)
                    .build();
                comCashbackCampaignRepo.saveAndFlush(comCashbackCampaign);
                context.put(Constant.KeyVar.REGISTER_LOYALTY, Constant.STATUS_FAILED);
            }
            comRegisterLoyaltyLogRepo.saveAndFlush(registerLoyalty);
        }
        catch (Exception e) {
            AppLog.error("ERROR", e);
        }
        context.setResult(result);
        return !result.isOk();
    }

}
