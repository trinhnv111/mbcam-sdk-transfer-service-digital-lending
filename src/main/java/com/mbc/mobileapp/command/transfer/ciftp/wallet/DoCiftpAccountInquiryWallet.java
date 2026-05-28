package com.mbc.mobileapp.command.transfer.ciftp.wallet;

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
import com.mbc.mobileapp.command.transfer.ciftp.RegexPattern;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class DoCiftpAccountInquiryWallet implements Command {

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
            input.setService(CommonServiceConstant.Service.CASA_TO_WALLET);
            if (!StringUtils.isEmpty(request.getPhoneNo())) {
                if (!request.getAccountId().startsWith("855") || !RegexPattern.validateAccountId(request.getAccountId())) {
                    result = new SimpleResult(MBCResponseCode.INVALID_ACCOUNT_ID.getDesc(), false,
                            MBCResponseCode.INVALID_ACCOUNT_ID.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                input.setAccountNumber(request.getAccountId());
                input.setSubType(CommonServiceConstant.SubType.PHONE.name());

            } else {
                input.setAccountNumber(request.getAccountId());
                input.setSubType(CommonServiceConstant.SubType.ACCOUNT_ID.name());
            }


            ExecuteT24Output<CiftpAccountInquiryOutput> output = callFundsTransferService.getInfoAccountToWallet(input, customer.getId(), request.getRequestId());
            if (output != null) {
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                    if ("rejected".equalsIgnoreCase(output.getData().getKycStatus()) || "unknown".equalsIgnoreCase(output.getData().getKycStatus())) {
                        result = new SimpleResult(MBCResponseCode.INVALID_KYC_STATUS.getDesc(), false,
                                MBCResponseCode.INVALID_KYC_STATUS.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    } else if (output.getData().getFrozen()) {
                        result = new SimpleResult(MBCResponseCode.USER_BLOCK.getDesc(), false,
                                MBCResponseCode.USER_BLOCK.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }else if(Objects.nonNull(output.getData().getAccountStatus()) 
                        && "deactivated".equals(output.getData().getAccountStatus().toLowerCase())) {
                        result = new SimpleResult(MBCResponseCode.CIFTP_DESTINATION_ACCT_INACTIVE.getDesc(), false,
                            MBCResponseCode.CIFTP_DESTINATION_ACCT_INACTIVE.getCode());
                    }

                    response.setCiftpAccountInfo(output.getData());
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
            } else {
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                        ResponseCode.REQUEST_TIMEOUT.getCode());
            }

        } catch (Exception e) {
            log.error("[Exception check info CIFTP to wallet] requestId: {} desc: {}", request.getRequestId(), e.getStackTrace());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());

        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
