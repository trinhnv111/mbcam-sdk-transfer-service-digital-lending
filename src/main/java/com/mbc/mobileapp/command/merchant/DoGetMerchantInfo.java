package com.mbc.mobileapp.command.merchant;

import com.mbc.common.api.CallMerchantService;
import com.mbc.common.api.models.merchant.GetMerchantInfoInput;
import com.mbc.common.api.models.merchant.MerchantInfo;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DoGetMerchantInfo implements Command {

    @Autowired
    private CallMerchantService callMerchantService;


    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        try {
            GetMerchantInfoInput input = new GetMerchantInfoInput();
            input.setMerchantId(request.getMerchantId());

//            ExecuteT24Output<InfoMerchantAccountOutput> output = apiMerchantManager.getInfoAccountMerchant(input, customer.getId(), request.getRequestId());
            ExecuteT24Output<MerchantInfo> output = callMerchantService.getMerchantInfo(input, customer.getId(), request.getRequestId());
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                MerchantInfo merchantInfo = output.getData();

                if(merchantInfo.getAccNumber().size() > 2){
                    result = new SimpleResult(MBCResponseCode.QR_INVALID.getDesc(), false,
                            MBCResponseCode.QR_INVALID.getCode());
                }else{
                    response.setMerchantInfo(merchantInfo);
                }
            } else {
                if ("4088".equals(output.getErrorInfo().getErrorCode())) {
                    result = new SimpleResult(MBCResponseCode.QR_INVALID.getDesc(), false,
                            MBCResponseCode.QR_INVALID.getCode());
                }else {
                    String errorDesc = output.getErrorInfo().getErrorDesc();
                    if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                        errorDesc = output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                    }
                    result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());
                }
            }

        } catch (Exception e) {
            AppLog.error("[Exception Get Merchant Info] requestId: " +request.getRequestId() + " data: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());

        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
