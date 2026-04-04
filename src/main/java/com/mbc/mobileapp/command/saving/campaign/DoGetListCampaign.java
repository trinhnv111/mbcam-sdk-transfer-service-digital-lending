package com.mbc.mobileapp.command.saving.campaign;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComCampaignConfig;
import com.mbc.common.entity.ComCampaignDeposit;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlSavingRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.repository.ComCampaignConfigExtendRepo;
import com.mbc.mobileapp.repository.ComCampaignDepositExtendRepo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.saving.campaign.CampaignConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoGetListCampaign implements Command {
    
    @Autowired
    private ComCampaignConfigExtendRepo comCampaignConfigExtendRepo; 
    
    @Autowired
    private ComCampaignDepositExtendRepo comCampaignDepositExtendRepo;
    
    @Autowired
    private ComTransDtlSavingRepo comTransDtlSavingRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        Validator.Result result = Validator.Result.OK;
        CustInfo custInfo = context.getCustomer();
        
        try {
            String resident = "KH".equals(custInfo.getResident()) ? "Y" : "N";
            List<CampaignConfig> lstCampaignConfigs = new ArrayList<CampaignConfig>();
            List<ComCampaignConfig> lstCampaign = comCampaignConfigExtendRepo
                    .getListCampaignByChannelAndPartnerCode("DEPOSIT", request.getDigitalChannel(), request.getPartnerSdk());
            if(!lstCampaign.isEmpty()) {
                for (ComCampaignConfig comCampaignConfig : lstCampaign) {                                      
                    List<ComCampaignDeposit> lstComCampaignDeposit = comCampaignDepositExtendRepo
                        .findByDepositCampaignId(comCampaignConfig.getId()).stream().filter(e -> resident.equals(e.getResident()))
                        .collect(Collectors.toList());
                    
                    CampaignConfig campaignConfig = CampaignConfig.builder()
                        .id(comCampaignConfig.getId())
                        .campaignCode(comCampaignConfig.getCampaignCode())
                        .campaignName(comCampaignConfig.getCampaignName())
                        .channel(comCampaignConfig.getChannel())
                        .description(comCampaignConfig.getDescription())
                        .endDate(comCampaignConfig.getEndDate())
                        .startDate(comCampaignConfig.getStartDate())
                        .noDec(comCampaignConfig.getNoDec())
                        .partnerCode(comCampaignConfig.getPartnerCode())
                        .productType(comCampaignConfig.getProductType())
                        .status(comCampaignConfig.getStatus())
                        .volume(comCampaignConfig.getVolume())
                        .detailCampaign(lstComCampaignDeposit)
                        .build();
                    lstCampaignConfigs.add(campaignConfig);
                }
            }
            
            //check volume
            if(lstCampaignConfigs.size() > 0) {
                for (int i = 0; i < lstCampaignConfigs.size(); i++) {
                    CampaignConfig campaignConfig = lstCampaignConfigs.get(i);
                    int numberOfUsed = comTransDtlSavingRepo.numberOfCampaignCodeApply(campaignConfig.getCampaignCode());
                    if(Objects.nonNull(campaignConfig.getVolume())){
                        if(numberOfUsed >= Double.valueOf(campaignConfig.getVolume())) {
                            lstCampaignConfigs.remove(i);
                        }
                    }
                }
            }
            
            response.setLstCampaignSaving(lstCampaignConfigs);
        }
        catch (Exception e) {
            AppLog.error("[EXCEPTION GET LIST CAMPAIGN SAVING] requestId: "+request.getRequestId()+" desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

}
