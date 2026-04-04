package com.mbc.mobileapp.command.user.initsdk;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.dto.PartnerSdkResponse;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComPartnerSdkRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.user.initsdk.InitSdkInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoCheckPartner implements Command {

    private final ComPartnerSdkRepo comPartnerSdkRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        // context.getResponse();
        CustInfo custInfo = null;
        InitSdkInfo initSdkInfo = request.getInitSdkInfo();

        try {
            List<PartnerSdkResponse> comPartnerSdk = comPartnerSdkRepo.getPartner(List.of("ACTIVE"), null, initSdkInfo.getPartner(), null);
            if (comPartnerSdk.isEmpty()
                    || !Constant.ACCT_STATUS_ACTIVE.equals(comPartnerSdk.get(0).getStatus().name())) {
                result = new SimpleResult(MBCResponseCode.PARTNER_INVALID.getDesc(), false, MBCResponseCode.PARTNER_INVALID.getCode());
                context.setResponse(response);
                context.setResult(result);
                return !result.isOk();
            }

            response.setPartnerSdk(comPartnerSdk.get(0));
            response.setChannel("SDK.RETAIL");

        } catch (Exception e) {
            AppLog.error("[SDK Exception Init] requestId: " + request.getRequestId() + " desc: ", e);
            result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
