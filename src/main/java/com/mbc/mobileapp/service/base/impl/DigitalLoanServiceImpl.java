package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementInformationResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.ValidDisbursementResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetLoanResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetPaymentHistoryResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetPdResponse;
import com.mbc.mobileapp.rest.digitalloan.repayment.LoanRepaymentResponse;
import com.mbc.mobileapp.service.base.DigitalLoanService;
import com.mbc.mobileapp.service.digital_loan.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalLoanServiceImpl extends ServiceBase implements DigitalLoanService {
    private final GetLoanService getLoanService;
    private final GetPdLoanService getPdLoanService;
    private final GetPaymentHistoryService getPaymentHistoryService;
    private final LoanRepaymentService loanRepaymentService;
    private final LoanValidDisbursementService loanValidDisbursementService;
    private final LoanDisbursementService loanDisbursementService;
    private final LoanDisbursementInformation loanDisbursementInformation;

    @Override
    public GetLoanResponse getLoan(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        GetLoanResponse response = new GetLoanResponse();
        Validator.Result result;
        try {
            getLoanService.execute(context);
            logService.execute(context);
            result = context.getResult();
            response.setResult(result);
            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) context.getResponse();
                response.setData(res.getLoanOutput());
                response.setT24DayNow(res.getT24DayNow());
            }
        } catch (Exception e) {
            log.error(e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;
    }

    @Override
    public GetPdResponse getPd(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        GetPdResponse response = new GetPdResponse();
        Validator.Result result;
        try {
            getPdLoanService.execute(context);
            logService.execute(context);
            result = context.getResult();
            response.setResult(result);
            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) context.getResponse();
                response.setData(res.getPdOutput());
            }
        } catch (Exception e) {
            log.error(e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;
    }

    @Override
    public GetPaymentHistoryResponse getPaymentHistory(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);

        GetPaymentHistoryResponse response = new GetPaymentHistoryResponse();
        Validator.Result result = null;
        try {
            getPaymentHistoryService.execute(context);
            logService.execute(context);

            result = context.getResult();
            response.setResult(result);

            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) context.getResponse();
                response.setData(res.getPaymentHistoryOutput());
            }
        } catch (Exception e) {
            log.error(e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;
    }

    @Override
    public LoanRepaymentResponse repayment(CommonServiceRequest request, CustInfo cust, TokenOtp otp) {
        ProcessContext context = loadContext(request, cust);
        context.putVar(Constant.KeyVar.OTP, otp);
        LoanRepaymentResponse repaymentResponse = new LoanRepaymentResponse();
        Validator.Result result;
        try {
            loanRepaymentService.execute(context);
            logService.execute(context);
            result = context.getResult();
            repaymentResponse.setResult(result);
            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) context.getResponse();
                repaymentResponse.setData(res.getRepaymentInfo());
            }
        } catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        return repaymentResponse;
    }

    @Override
    public ValidDisbursementResponse validDisbursement(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
//        context.putVar(Constant.KeyVar.OTP, tokenOtp);
        ValidDisbursementResponse response = new ValidDisbursementResponse();
        Validator.Result result;
        try {
            loanValidDisbursementService.execute(context);
            logService.execute(context);
            result = context.getResult();
            response.setResult(result);
            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) context.getResponse();
                response.setData(res.getValidDisbursementResponse().getData());
            }
        } catch (Exception e) {
            log.error(e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;


    }


    @Override
    public DisbursementResponse<Object> disbursement(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
//        context.putVar(Constant.KeyVar.OTP, tokenOtp);
        DisbursementResponse<Object> response = new DisbursementResponse<>();
        Validator.Result result;
        try {
            loanDisbursementService.execute(context);
            logService.execute(context);
            result = context.getResult();
            response.setResult(result);
            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) context.getResponse();

                // Đóng gói data trả FE: ft (Chặng 2) + ciftpStatus (Chặng 3, nếu có)
                java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
                data.put("ft", res.getFt());

                Object ciftpStatus = context.get("ciftp_status");
                if (ciftpStatus != null) {
                    data.put("ciftpStatus", ciftpStatus);
                }
                Object ciftpFt = context.get("ciftp_ft");
                if (ciftpFt != null) {
                    data.put("ciftpFt", ciftpFt);
                }

                response.setData(data);
            }
        } catch (Exception e) {
            log.error(e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;
    }


    @Override
    public DisbursementInformationResponse disbursementInformation(CommonServiceRequest request, CustInfo cust){
        DisbursementInformationResponse  response = new DisbursementInformationResponse();
        ProcessContext context = loadContext(request, cust);
        Validator.Result result;
        try {
            loanDisbursementInformation.execute(context);
            logService.execute(context);
            result = context.getResult();
            response.setResult(result);
            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) context.getResponse();
                if (res.getDisbursementInformationResponse() != null) {
                    response.setData(res.getDisbursementInformationResponse().getData());
                }
                response.setResult(context.getResult());
            }
        } catch (Exception e) {
            log.error(e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;

    }

    @Override
    public DisbursementResponse<Object> genFile(CommonServiceRequest request, CustInfo cust) {
        //TODO
        return null;
    }

}
