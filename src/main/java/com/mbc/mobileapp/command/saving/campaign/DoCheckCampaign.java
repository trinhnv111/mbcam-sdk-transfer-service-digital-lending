package com.mbc.mobileapp.command.saving.campaign;


import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComCampaignConfig;
import com.mbc.common.entity.ComCampaignDeposit;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlSavingRepo;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.repository.ComCampaignConfigExtendRepo;
import com.mbc.mobileapp.repository.ComCampaignDepositExtendRepo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.saving.open.SavingInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoCheckCampaign implements Command {

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
            SavingInfo savingDepositInfo = request.getSavingInfo();
            String resident = "KH".equals(custInfo.getResident()) ? "Y" : "N";

            if(!Utility.isNull(savingDepositInfo.getCampaignCode())) {

                List<ComCampaignConfig> lstCampaign = comCampaignConfigExtendRepo
                        .getListCampaignByCampaignCodeAndChannelAndPartnerCode( savingDepositInfo.getCampaignCode(),"DEPOSIT",
                                request.getDigitalChannel(), request.getPartnerSdk());

                if(!lstCampaign.isEmpty()) {
                    ComCampaignConfig comCampaignConfig = lstCampaign.get(0);
                    savingDepositInfo.setCampaignVolume(comCampaignConfig.getVolume());
                    List<ComCampaignDeposit> lstComCampaignDeposit = comCampaignDepositExtendRepo
                        .findByDepositCampaignId(comCampaignConfig.getId()).stream()
                        .filter(e ->
                        request.getSavingProductCode().equals(e.getProductCode())
                            && savingDepositInfo.getCurrency().equals(e.getCurrency())
                            && savingDepositInfo.getTerm().equals(e.getTerms())
                            && resident.equals(e.getResident())).collect(Collectors.toList());
                    if(!lstComCampaignDeposit.isEmpty()) {
                        savingDepositInfo.setCampaignInterest(lstComCampaignDeposit.get(0).getInterestRateExtra());
                    }

                    //check volume campaign
                    if(Objects.nonNull(comCampaignConfig.getVolume())) {
                        int numberApply = comTransDtlSavingRepo.numberOfCampaignCodeApply(savingDepositInfo.getCampaignCode()) + 1;
                        if(Double.valueOf(comCampaignConfig.getVolume()) < numberApply) {
                            savingDepositInfo.setCampaignCode(null);
                            savingDepositInfo.setCampaignInterest(null);
                        }
                    }
                }
            }
            request.setSavingInfo(savingDepositInfo);

        }
        catch (Exception e) {
            log.error("[EXCEPTION GET LIST CAMPAIGN SAVING] requestId: {} desc: {}", request.getRequestId(), e.getStackTrace());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

}
