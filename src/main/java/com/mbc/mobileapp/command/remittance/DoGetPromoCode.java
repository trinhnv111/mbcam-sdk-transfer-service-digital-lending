package com.mbc.mobileapp.command.remittance;

import java.util.Arrays;
import java.util.List;

import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.repository.ComCampaignConfigRepo;
import com.mbc.common.repository.ComPromoCodeRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.validator.base.Validator;
import com.mbc.common.validator.base.Validator.Result;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;


@Service
public class DoGetPromoCode implements Command {

    @Autowired
    private ComCampaignConfigRepo comCampaignConfigRepo;

    @Autowired
    private ComPromoCodeRepo comPromoCodeRepo;

    @Value("${remittance.promo.code}")
    private String promoCode;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Result.OK;
        String requestId = context.getRequest().getRequestId();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        try {
//            List<ComCampaignConfig> lstCampaign = comCampaignConfigRepo.getListCampaignByProductTypeAndStatus("REMITTANCE", Constant.USER_STATUS_ACTIVE);
//            if (lstCampaign.size() == 1) {
//                ComCampaignConfig comCampaignConfig = lstCampaign.get(0);
//                ComPromoCode comPromoCode = comPromoCodeRepo.findByCodeAndStatusAndCampaignCode(
//                    initMakeTransferInfo.getPromoCode(), Constant.STATUS_1, comCampaignConfig.getCampaignCode());
//                if (comPromoCode == null) {
//                    result = new SimpleResult(MBCResponseCode.REMITTANCE_PROMO_CODE_NOT_EXIST.getDesc(), false,
//                        MBCResponseCode.REMITTANCE_PROMO_CODE_NOT_EXIST.getErrorCode());
//                }
//            }else {
//                result = new SimpleResult(MBCResponseCode.REMITTANCE_PROMO_CODE_NOT_EXIST.getDesc(), false,
//                    MBCResponseCode.REMITTANCE_PROMO_CODE_NOT_EXIST.getErrorCode());
//            }

            List<String> lstPromoCode = Arrays.asList(promoCode.split(","));
            response.setLstPromoCode(lstPromoCode);

        }
        catch (Exception e) {
            AppLog.error("[ERROR Do Check Promo Code] requestId: " + requestId + " desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }

}
