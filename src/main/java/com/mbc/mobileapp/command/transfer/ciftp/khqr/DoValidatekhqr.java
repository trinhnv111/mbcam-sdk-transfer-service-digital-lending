package com.mbc.mobileapp.command.transfer.ciftp.khqr;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import kh.org.nbc.bakong_khqr.BakongKHQR;
import kh.org.nbc.bakong_khqr.model.CRCValidation;
import kh.org.nbc.bakong_khqr.model.KHQRResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

@Service
public class DoValidatekhqr implements Command {
    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CustInfo customer = context.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        TransInfo khqrTransInfo = request.getTransInfo();

        try {
//            if (!Utility.isNull(khqrTransInfo.getPayloadQr())) {
//                KHQRResponse<CRCValidation> khqrResponse = BakongKHQR.verify(khqrTransInfo.getPayloadQr());
//                if (!khqrResponse.getData().isValid()) {
//                    result = new SimpleResult(MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getDesc(), false,
//                            MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getCode());
//                    context.setResult(result);
//                    return !result.isOk();
//                }
//
//            }else{
                result = new SimpleResult(MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getDesc(), false,
                        MBCResponseCode.BAKONG_PAYLOAD_QR_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
//            }

        }catch (Exception e){
            AppLog.error("[SDK Exception Validate KHQR] requestId: "+request.getRequestId()+ " desc: {}" , e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
