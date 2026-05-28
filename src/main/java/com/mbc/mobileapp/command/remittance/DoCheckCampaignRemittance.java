package com.mbc.mobileapp.command.remittance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.entity.ComCampaignCashbackConf;
import com.mbc.common.entity.ComCampaignConfig;
import com.mbc.common.entity.ComCashbackNumTrx;
import com.mbc.common.entity.ComTransDtlInternational;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComCampaignCashbackConfRepo;
import com.mbc.common.repository.ComCampaignConfigRepo;
import com.mbc.common.repository.ComCashbackNumTrxRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DoCheckCampaignRemittance implements Command {
    
    @Autowired
    private ComCampaignConfigRepo comCampaignConfigRepo;
    
    @Autowired
    private ComCampaignCashbackConfRepo comCampaignCashbackConfRepo;
    
    @Autowired
    private ComCashbackNumTrxRepo comCashbackNumTrxRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = context.getResult();

        CustInfo custInfo = context.getCustomer();
        String requestId = context.getRequest().getRequestId();
        ComTransDtlInternational comTransDtlInternational = (ComTransDtlInternational) context.getVar("ComTransDtlInternational");
        
        try {
            log.info("[CHECK CAMPAIGN REMITTANCE] requestId {}, DATA: {}", requestId, JSON.stringify(comTransDtlInternational));

            if(comTransDtlInternational != null) {
                if(Constant.COM_STATUS_COM.equals(comTransDtlInternational.getStatus())) {
                    
                    List<ComCampaignConfig> lstCampaign = comCampaignConfigRepo.getListCampaignByProductTypeAndStatus("REMITTANCE", Constant.USER_STATUS_ACTIVE);
                    if(lstCampaign.size() == 1) {
                        ComCampaignConfig comCampaignConfig = lstCampaign.get(0);
                        ComCampaignCashbackConf comCampaignCashbackConf = comCampaignCashbackConfRepo.findByCampaignCode(comCampaignConfig.getCampaignCode());
                        if("mobile".equals(comCampaignConfig.getChannel()) 
                            && comTransDtlInternational.getDebitAmount().compareTo(comCampaignCashbackConf.getMinTransAmount()) >= 0
                            && comTransDtlInternational.getDebitCurrency().equals(comCampaignCashbackConf.getMinTransCurrency())
                            && !Utility.isNull(comTransDtlInternational.getPromoCode())) 
                        {
                            log.info("[Number Transaction Remittance COM] requestId {}, desc: {}", requestId, comTransDtlInternational.getId());
                            ComCashbackNumTrx comCashbackNumTrx = comCashbackNumTrxRepo.findByCustomerCodeAndCampaignCode(custInfo.getHostCifId(), comCampaignConfig.getCampaignCode());
                            if(Objects.isNull(comCashbackNumTrx)) {
                                comCashbackNumTrx = ComCashbackNumTrx.builder()
                                    .campaignCode(comCampaignConfig.getCampaignCode())
                                    .customerCode(custInfo.getHostCifId())
                                    .numTrxRemittance(BigDecimal.ONE)
                                    .build();
                            }else {
                                comCashbackNumTrx.setNumTrxRemittance(comCashbackNumTrx.getNumTrxRemittance().add(BigDecimal.ONE));
                            }
                            
                            comCashbackNumTrxRepo.saveAndFlush(comCashbackNumTrx);
                        }
                    }
                }
                
                if(Constant.COM_STATUS_PND.equals(comTransDtlInternational.getStatus())) {
                    List<ComCampaignConfig> lstCampaign = comCampaignConfigRepo.getListCampaignByProductTypeAndStatus("REMITTANCE", Constant.USER_STATUS_ACTIVE);
                    if(lstCampaign.size() == 1) {
                        ComCampaignConfig comCampaignConfig = lstCampaign.get(0);
                        ComCampaignCashbackConf comCampaignCashbackConf = comCampaignCashbackConfRepo.findByCampaignCode(comCampaignConfig.getCampaignCode());
                        if("mobile".equals(comCampaignConfig.getChannel()) 
                            && comTransDtlInternational.getDebitAmount().compareTo(comCampaignCashbackConf.getMinTransAmount()) >= 0
                            && comTransDtlInternational.getDebitCurrency().equals(comCampaignCashbackConf.getMinTransCurrency())
                            && !Utility.isNull(comTransDtlInternational.getPromoCode())) 
                        {
                            log.info("[Number Transaction Remittance PND] requestId {}, desc: {}", requestId, comTransDtlInternational.getId());
                            ComCashbackNumTrx comCashbackNumTrx = comCashbackNumTrxRepo.findByCustomerCodeAndCampaignCode(custInfo.getHostCifId(), comCampaignConfig.getCampaignCode());
                            if(Objects.isNull(comCashbackNumTrx)) {
                                comCashbackNumTrx = ComCashbackNumTrx.builder()
                                    .campaignCode(comCampaignConfig.getCampaignCode())
                                    .customerCode(custInfo.getHostCifId())
                                    .numTrxRemittance(BigDecimal.ONE)
                                    .numCashbackTopup(BigDecimal.ONE)
                                    .numCashbackBus(BigDecimal.ONE)
                                    .numCashbackReferral(BigDecimal.ONE)
                                    .build();
                            }else {
                                comCashbackNumTrx.setNumTrxRemittance(comCashbackNumTrx.getNumTrxRemittance().add(BigDecimal.ONE));
                                comCashbackNumTrx.setNumCashbackTopup(comCashbackNumTrx.getNumCashbackTopup() == null ? BigDecimal.ONE : comCashbackNumTrx.getNumCashbackTopup().add(BigDecimal.ONE));
                                comCashbackNumTrx.setNumCashbackBus(comCashbackNumTrx.getNumCashbackBus() == null ? BigDecimal.ONE : comCashbackNumTrx.getNumCashbackBus().add(BigDecimal.ONE));
                                comCashbackNumTrx.setNumCashbackReferral(comCashbackNumTrx.getNumCashbackReferral() == null ? BigDecimal.ONE : comCashbackNumTrx.getNumCashbackReferral().add(BigDecimal.ONE));
                            }
                            
                            comCashbackNumTrxRepo.saveAndFlush(comCashbackNumTrx);
                        }
                    }
                }
                
            }
            
        }
        catch (Exception e) {
            log.error("[Exception Save Number Transaction Remittance] requestId {}, desc: {}",requestId, e.getStackTrace());
//            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
//                    ResponseCode.TRANSACTION_FAIL.getCode());
           
        }
        
//        context.setResult(result);
        return result.isOk();
    }

}
