package com.mbc.mobileapp.command.transfer.khqr;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import com.mbc.mobileapp.service.transfer.ciftp.wallet.CiftpExecuteToWalletService;
import com.mbc.mobileapp.service.transfer.inhouse.MakeTransferService;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DoKHQRExecuteTransfer implements Command {

    @Autowired
    private MakeTransferService makeTransferService;

    @Autowired
    private CiftpExecuteToWalletService ciftpExecuteToWalletService;

    @Autowired
    private ComTransRepo comTransRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String custId = customer.getId();
        String transId = request.getTransId();
        String srvcCd = request.getSrvcCd();

        try{
            ComTrans comTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionIdAndChannelAndPartnerCode
                    (custId, srvcCd, Constant.COM_STATUS_INT, transId, request.getSessionId(), request.getDigitalChannel(), request.getPartnerSdk());
            if (Objects.nonNull(comTrans)){
                if(CommonServiceConstant.TransferType.INHOUSE.name().equals(comTrans.getTransferType())){
                    makeTransferService.execute(context);
                    result = context.getResult();
                }

                if(CommonServiceConstant.TransferType.CIFTP.name().equals(comTrans.getTransferType())){
                    ciftpExecuteToWalletService.execute(context);
                    result = context.getResult();
                }
            }else{
                AppLog.error("[Info Trans Invalid] requestId: "+request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_ID_INCORRECT.getDesc(), false,
                        ResponseCode.TRANSACTION_ID_INCORRECT.getCode());
            }


        }catch (Exception e){
            AppLog.error("[Exception Execute Transfer KHQR] requestId: "+request.getRequestId()+", desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setRequest(request);
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();

    }
}
