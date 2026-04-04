package com.mbc.mobileapp.command.transfer.ciftp;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallFundsTransferService;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpAccountInQuiryInput;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpAccountInquiryOutput;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class DoCiftpAccountInquiryCasa implements Command {

    @Autowired
    private CallFundsTransferService callFundsTransferService;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        try {
            CiftpAccountInQuiryInput input = new CiftpAccountInQuiryInput();
            input.setTransferType(CommonServiceConstant.TransferType.CIFTP.name());
            input.setService(CommonServiceConstant.Service.CASA_TO_CASA);
            input.setAccountNumber(request.getAcctNo());
            input.setParticipantCode(request.getParticipantCode());

            ExecuteT24Output<CiftpAccountInquiryOutput> output = callFundsTransferService.getInfoAccountToCasa(input, customer.getId(), request.getRequestId());
            
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {               
                if(Objects.nonNull(output.getData().getAccountStatus()) 
                    && "deactivated".equals(output.getData().getAccountStatus().toLowerCase())) {
                    result = new SimpleResult(MBCResponseCode.CIFTP_DESTINATION_ACCT_INACTIVE.getDesc(), false,
                        MBCResponseCode.CIFTP_DESTINATION_ACCT_INACTIVE.getCode());
                }else {
                    response.setCiftpAccountInfo(output.getData());
                }

            } else if ("400".equals(output.getStatus()) && "65".equals(output.getSoaErrorCode())) {
                result = new SimpleResult(MBCResponseCode.ACCOUNT_BENEFICIARY_INCORRECT.getDesc(), false,
                        MBCResponseCode.ACCOUNT_BENEFICIARY_INCORRECT.getCode());
            } else if ("400".equals(output.getStatus()) && "182".equals(output.getSoaErrorCode())) {
                result = new SimpleResult(MBCResponseCode.BANK_OFF_QUERY.getDesc(), false,
                        MBCResponseCode.BANK_OFF_QUERY.getCode());
            } else {
                String errorDesc = output.getErrorInfo().getErrorDesc();
                if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                    errorDesc = output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                }
                result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());
            }

        } catch (Exception e) {
            log.error("[Exception check info CIFTP to casa] requestId: {} desc: {}", request.getRequestId(), e.getStackTrace());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());

        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
