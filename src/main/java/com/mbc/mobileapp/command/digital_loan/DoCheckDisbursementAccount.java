package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.AcctRepo;
import com.mbc.common.util.JSON;
import com.mbc.common.validator.base.Validator.Result;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.CommonResponseCode;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DoCheckDisbursementAccount implements Command {

    @Autowired
    private AcctRepo acctRepo;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Result result = Result.OK;
        CustInfo custInfo = processContext.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        try {
//            Acct acct = acctRepo.findByAcctNo();
//            if (Objects.isNull(acct) || acct.getInactiveSts().equals(Constant.STATUS_1)
//                || !custInfo.getHostCifId().equals(acct.getHostCustId())
//                || !acct.getCcyCd().equals(overdraftInfo.getCrebitAcctCcy())) {
//                result = new SimpleResult(CommonResponseCode.DISBURSEMENT_ACCOUNT_IS_INVALID.getErrorCode(), false,
//                    CommonResponseCode.DISBURSEMENT_ACCOUNT_IS_INVALID.getErrorDesc());
//            }
        } catch (Exception e) {
            log.error("[Exception valid disbursement account] requestId: {} desc: {}", request.getRequestId(), JSON.stringify(e));
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    ResponseCode.TRANSACTION_FAIL.getDesc());
        }
        processContext.setResult(result);
        return !result.isOk();
    }

}
