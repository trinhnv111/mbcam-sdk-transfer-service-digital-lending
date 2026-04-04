package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.Request;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.transfer.MakeTransferResponse;
import com.mbc.mobileapp.rest.transfer.TransInfoResponse;
import com.mbc.mobileapp.rest.transfer.banklist.ListBankCiftpResponse;
import com.mbc.mobileapp.rest.transfer.ciftp.AccountInquiryResponse;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRCheckInfoResponse;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRMakeTransferResponse;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRTransferResponse;


public interface TransferService {

    public TransInfoResponse validateTransfer(Request request, CustInfo cust);

    public MakeTransferResponse makeTransfer(Request request, CustInfo cust, TokenOtp otp);

    public ListBankCiftpResponse getListBankCiftp(Request request, CustInfo cust);

    public AccountInquiryResponse ciftpAccountInquiryCasaService(Request request, CustInfo cust);

    public AccountInquiryResponse ciftpAccountInquiryWalletService(Request request, CustInfo cust);

    public KHQRCheckInfoResponse checkInfoKHQRTransfer(Request request, CustInfo cust);

    public KHQRTransferResponse validKHQRTransfer(Request request, CustInfo cust);

    public KHQRMakeTransferResponse executeKHQRTransfer(Request request, CustInfo cust, TokenOtp otp);

}
