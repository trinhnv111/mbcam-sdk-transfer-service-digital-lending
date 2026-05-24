package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.Request;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementInformationResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.ValidDisbursementResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetLoanResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetPaymentHistoryResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetPdResponse;
import com.mbc.mobileapp.rest.digitalloan.repayment.LoanRepaymentResponse;

public interface DigitalLoanService {
    GetLoanResponse getLoan(CommonServiceRequest request, CustInfo cust);

    GetPdResponse getPd(CommonServiceRequest request, CustInfo cust);

    GetPaymentHistoryResponse getPaymentHistory(CommonServiceRequest request, CustInfo cust);

    LoanRepaymentResponse repayment(CommonServiceRequest request, CustInfo cust, TokenOtp otp);

    ValidDisbursementResponse validDisbursement(CommonServiceRequest request, CustInfo cust);

    DisbursementResponse<Object> disbursement(Request request, CustInfo cust);

    DisbursementInformationResponse disbursementInformation(CommonServiceRequest request, CustInfo cust);

    DisbursementResponse<Object> genFile(CommonServiceRequest request, CustInfo cust);

}
