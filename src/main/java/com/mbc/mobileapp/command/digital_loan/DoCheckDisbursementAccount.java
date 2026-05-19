package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.AcctRepo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.common.validator.base.Validator.Result;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallMsILService;
import com.mbc.mobileapp.api.model.digitalloan.output.ILOutput;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoGetCustInfoFromEM;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoValidateSalaryCust;
import com.mbc.mobileapp.constant.CommonResponseCode;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.disbursement.ValidDisbursementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor

public class DoCheckDisbursementAccount implements Command {
    //    @Autowired
//    private AcctRepo acctRepo;
    private final ComTransDtlLmtRepository comTransDtlLmtRepository;
    private final DoGetCustInfoFromEM doGetCustInfoFromEM;
    private final DoValidateSalaryCust doValidateSalaryCust;
    private final CallMsILService callMsILService;
    private ComTransDtlLmt comTransDtlLmtNotFound;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Result result = Result.OK;
        CustInfo custInfo = processContext.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        ValidDisbursementRequest validDisbursementRequest = request.getValidDisbursementRequest();
        try {

            ComTransDtlLmt comTransDtlLmt = comTransDtlLmtRepository.findById(validDisbursementRequest.getTransId())
                    .orElseThrow(() ->
                            new RuntimeException("ComTransDtlLmt not found with transId: " + validDisbursementRequest.getTransId()));

            LocalDate today = LocalDate.now();

            LocalDate valueDate = comTransDtlLmt.getLimitValueDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalDate endDate = comTransDtlLmt.getLimitEndDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();


            if (!today.equals(valueDate) && !today.isAfter(endDate)) {
                // call eM check tương tự limit
                log.info("[DoCheckDisbursementAccount] ngày yêu cầu giải ngân khác ngày cấp hạn mức ");

                boolean isGetCustomer = doGetCustInfoFromEM.execute(context);
                if (isGetCustomer) return true; // Get Customer lỗi (timeout, error...)

                boolean isValidate = doValidateSalaryCust.execute(context);
                if (isValidate) return true; // Validate không pass
            }
            if (today.equals(valueDate)) {
                if (custInfo != null) {
                    log.info("[DoCheckDisbursementAccount] ngày yêu cầu giải ngân bằng ngày cấp hạn mức call list account ");
                    NonSavingAcctInput inputMessage = new NonSavingAcctInput();
                    inputMessage.setCustomerId(custInfo.getHostCifId());
                    inputMessage.setProcessingCode(new String[]{"0002"});

                    // call list account
                    ExecuteT24Output<List<ILOutput>> iLResponse = callMsILService.getSavingAccountListV2(inputMessage, custInfo.getId(), request.getRequestId());

                    // Xử lý dữ liệu trả về từ IL
                    if (iLResponse != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(iLResponse.getError())) {
                        List<ILOutput> accountList = iLResponse.getData();

                        if (accountList != null && !accountList.isEmpty()) {
                            log.info("[DoCheckDisbursementAccount] Lấy danh sách tài khoản thành công, số lượng: {}", accountList.size());
                        }
                    } else {
                        log.error("[DoCheckDisbursementAccount] Lỗi lấy danh sách tài khoản từ IL: {}", iLResponse != null ? iLResponse.getErrorDesc() : "null");
                    }
                }
            }
            if (today.isAfter(endDate)) {
                log.error("[Exception valid disbursement account today > limitEndDate] requestId: {}", request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        ResponseCode.TRANSACTION_FAIL.getDesc());
                processContext.setResult(result);
                return true;
            }

        } catch (Exception e) {
            log.error("[Exception valid disbursement account] requestId: {} desc: {}", request.getRequestId(), JSON.stringify(e));
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    ResponseCode.TRANSACTION_FAIL.getDesc());
        }
        processContext.setResult(result);
        return !result.isOk();
    }

}