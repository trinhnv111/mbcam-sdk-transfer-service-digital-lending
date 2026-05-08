package com.mbc.mobileapp.rest.account.bean;

import com.mbc.common.dto.District;
import com.mbc.common.dto.Province;
import com.mbc.common.dto.Ward;
import com.mbc.mobileapp.object.ProductSavingV2;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CommonServiceResponse {

//    private TokenOtp tokenOTP;

    private String token;
    
    private boolean ekycSuccess;
    
    private String hashBankId;
    
    private String bioId;

    private List<String> lstCurrency;
    
//    private ExecuteT24Output<CustomerInfoT24> customerInfo;
//
//    private List<AccountBase> lstNonSavingAccount;
//
//    private AcctNumberInfo acctNumberInfo;
//
//    private List<TransHistoryInfo> lstTransHistory;

    //=========== INIT SDK =============================================
//    private CustomerInfo customerInfoInitSdk;

    private String tid;

    private String partner;

    private String channel;

    //=========== SAVING =============================================
//    private  List<ProductSaving> lstProductSaving;

    private List<ProductSavingV2> lstProductSavingV2;

//    private List<AccountSaving> lstSavingAccount;
//
//    private List<InterestOutput> interestResponseV2;
//
//    private InterestOutput interestOutput;
//
//    private OpenSavingOutput openSavingOutput;
//
//    private DepositClosureOutput depositClosureOutput;
//
//    private CheckCoBOutput checkCoBOutput;
//
//    private List<CampaignConfig> lstCampaignSaving;
//
//    private TopUpSavingDepositOutput topUpSavingDepositOutput;



    //=========== REMITTANCE =============================================
//    private List<RemittanceBankListOutput> remittanceBankListOutputList;
//
//    private List<RemittanceAddressOutput> remittanceAddressOutput;
//
//    private GetAccountName getAccountNameOutput;
//
//    private RemittanceMakeTransferFinishOutput makeTransferFinishOutput;

    private List<String> lstPromoCode;



    //=========== TRANSFER =============================================
//    private List<CiftpBankInfo> lstCiftpBank;
//
//    private CiftpAccountInquiryOutput ciftpAccountInfo;
//
//    private List<CiftpMakeConfigInfo> lstMakeTransferConfig;

    private String transHash;

//    private GetDetailTransactionHistoryOutput getDetailTransactionHistoryOutput;

    private String qrPayType;

    private String transferType;

    private String bakongAcctId;

    //=========== MERCHANT =============================================
//    private MerchantInfo merchantInfo;



    //=========== RM =============================================
//    private RmCodeOutput rmCodeOutput;
//
//    private List<RmCodeOutput> lstRmCodeOutput;

    //=========== ADDRESS =============================================
//    public List<Country> lstCountry;

    public List<Province> lstProvince;

    public List<District> lstDistrict;

    public List<Ward> lstWard;

    //PARTNER SDK
//    private PartnerSdkResponse partnerSdk;
}
