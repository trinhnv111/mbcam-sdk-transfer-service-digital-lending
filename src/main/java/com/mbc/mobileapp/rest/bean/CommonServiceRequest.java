
package com.mbc.mobileapp.rest.bean;

import com.mbc.common.api.models.ekyc.CheckOcrMobileData;
import com.mbc.common.bean.Request;
import com.mbc.common.bean.TokenOtp;
import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetLoanRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.PaymentRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceInitRequest;
import com.mbc.mobileapp.rest.digitalloan.repayment.LoanRepaymentRequest;
import com.mbc.mobileapp.rest.remittance.finish.MakeTransferFinishRequest;
import com.mbc.mobileapp.rest.remittance.getaccountname.GetAccountNameRequest;
import com.mbc.mobileapp.rest.remittance.init.InitMakeTransferInfo;
import com.mbc.mobileapp.rest.saving.close.DepositClosureInfo;
import com.mbc.mobileapp.rest.saving.interest.InterestRequest;
import com.mbc.mobileapp.rest.saving.open.SavingInfo;
import com.mbc.mobileapp.rest.saving.topup.TopUpSavingInfo;
import com.mbc.mobileapp.rest.transfer.TransInfo;
import com.mbc.mobileapp.rest.user.initsdk.InitSdkInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class CommonServiceRequest extends Request {

    // user
    private String userId;

    private String acctNo;

    private String fromDate;

    private String toDate;

    private String date;

    private String customerNationId;

    private String custName;

    private String password;

    private String newPassword;

    private TokenOtp tokenOTP;

    private String phoneNo;

    private String softTokenId;

    private String digitalChannel;

    private String idTypNo;

    private String idCardType;

    private String fingerPrint;

    private String phoneId;

    private String pinCode;

    private String rePinCode;

    private String oldPinCode;

    private String deviceName;

    private String partnerSdk;

    private String addressCode;

    private String parentAddressCode;


//    private OpenCustomerInfo openCustomerInfo;
//
//    private AuthEkyc authEkyc;

    private String username;

    private String email;

    private String expectedString;

    private String branchCode;

    private String maxRecord;

    private String page;

    private String customerPiorityType;

    private String customerSegment;

    private String channel;

    private String accountLength;

    private String currency;

    private Boolean isRegisterFlow;

//    private CreateVipAcctInfo createVipAcctInfo;
//
//    private SavingAcctInfo savingAcctInfo;

    private String rmCode;

    private String rmMobile;

    private String eventCode;

    private String accountCategory;

    private String category;

//    private SavingTransInfo transInfo;

    //LICH SU GIAO DICH
    private String clientMessageId;

    private Date endPostingDate;

    private Date endTransactionDate;

    private int sizeResponse;

    private Date startPostingDate;

    private Date startTransactionDate;

    private String savingAcct;

    private String transCode;

    private String refNo;

    private String oriTerminalId;

    private String transactionType;
//
//    private SavingFixedDepositInfo savingFixedDepositInfo;
//
//    private DepositClosureInfo depositClosureInfo;

    //LOG ONBOARDING
    private CheckOcrMobileData checkOcrMobileData;

    private String createCif;

    private String createAcct;

    private String createEbank;

    private String ekycBinding;

    private String statusCheckAML;

    //topup deposit info
    private boolean isInterestRate;
    
//    private TopUpSavingDepositInfo topUpSavingDepositInfo;
    


//    private InterestRequest interestRequest;

    //=======USER=================================
    private InitSdkInfo initSdkInfo;

    private String tId;


    //=======REMITTANCE=================================
    private String type;

    private String code;

    private String promoCode;

    private GetAccountNameRequest getAccountNameRequest;

    private InitMakeTransferInfo initMakeTransferInfo;

    private MakeTransferFinishRequest makeTransferFinishRequest;


    //=======TRANSFER=================================
    private TransInfo transInfo;

    private String participantCode;

    private String ciftpTransferType;

    private String ciftpSettlement;

    private String accountId;

    private String payloadQr;

    //=======MERCHANT=================================
    private String merchantId;

    private String merchantName;


    //=======SAVING=================================
    private String savingProductCode;

    private InterestRequest interestRequest;

    private SavingInfo savingInfo;

    private DepositClosureInfo depositClosureInfo;

    private TopUpSavingInfo topUpSavingInfo;


    //=======BENEFICIARY=================================
    // THONG TIN NGUOI THU HUONG
    private String benAcctNo;

    private String benAcctName;

    private String benBankCode;

    private String benBankMnemonic;

    private String benBankName;

    private String suggestName;

    private String beneficiaryId;

    private String transType;

    //=======REGISTER EBANKING=================================

    private RegisterCustInfo registerCustInfo;

    private String registerCustId;

    //region Digital Lending Common
    private GetLoanRequest getLoanRequest;
    private PaymentRequest paymentRequest;
    private LoanRepaymentRequest loanRepaymentRequest;
    //endregion

    //region Salary Advance
    private SalaryAdvanceInitRequest salaryAdvanceInitRequest;
    //endregion

}
