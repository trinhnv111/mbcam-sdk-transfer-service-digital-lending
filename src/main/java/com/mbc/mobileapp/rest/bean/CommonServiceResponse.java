
package com.mbc.mobileapp.rest.bean;

import com.mbc.common.api.models.merchant.MerchantInfo;
import com.mbc.common.bean.Response;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.dto.*;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.mobileapp.api.model.account.transaction.history.bakong.GetDetailTransactionHistoryOutput;
import com.mbc.mobileapp.api.model.digitalloan.output.MsLoanGetPdOutput;
import com.mbc.mobileapp.api.model.digitalloan.output.PaymentHistoryOutPut;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceAddressOutput;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceBankListOutput;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceMakeTransferFinishOutput;
import com.mbc.mobileapp.api.model.rm.RmCodeOutput;
import com.mbc.mobileapp.api.model.salary_advance.output.CustInfoOutput;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.api.model.saving.close.DepositClosureOutput;
import com.mbc.mobileapp.api.model.saving.cob.CheckCoBOutput;
import com.mbc.mobileapp.api.model.saving.interest.InterestOutput;
import com.mbc.mobileapp.api.model.saving.open.OpenSavingOutput;
import com.mbc.mobileapp.api.model.saving.topup.TopUpSavingDepositOutput;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpAccountInquiryOutput;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpBankInfo;
import com.mbc.mobileapp.api.model.transfer.ciftp.CiftpMakeConfigInfo;
import com.mbc.mobileapp.object.ProductSavingV2;
import com.mbc.mobileapp.rest.account.AcctNumberInfo;
import com.mbc.mobileapp.rest.account.history.TransHistoryInfo;
import com.mbc.mobileapp.rest.digitalloan.repayment.RepaymentInfo;
import com.mbc.mobileapp.rest.remittance.getaccountname.GetAccountName;
import com.mbc.mobileapp.rest.saving.campaign.CampaignConfig;
import com.mbc.mobileapp.rest.user.initsdk.CustomerInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CommonServiceResponse extends Response {

    private TokenOtp tokenOTP;

    private String token;
    
    private boolean ekycSuccess;
    
    private String hashBankId;
    
    private String bioId;

    private List<String> lstCurrency;
    
    private ExecuteT24Output<CustomerInfoT24> customerInfo;
    
    private List<AccountBase> lstNonSavingAccount;

    private AcctNumberInfo acctNumberInfo;

    private List<TransHistoryInfo> lstTransHistory;

    //=========== INIT SDK =============================================
    private CustomerInfo customerInfoInitSdk;

    private String tid;

    private String partner;

    private String channel;

    //=========== SAVING =============================================
    private  List<ProductSaving> lstProductSaving;

    private List<ProductSavingV2> lstProductSavingV2;

    private List<AccountSaving> lstSavingAccount;

    private List<InterestOutput> interestResponseV2;

    private InterestOutput interestOutput;

    private OpenSavingOutput openSavingOutput;

    private DepositClosureOutput depositClosureOutput;

    private CheckCoBOutput checkCoBOutput;

    private List<CampaignConfig> lstCampaignSaving;

    private TopUpSavingDepositOutput topUpSavingDepositOutput;



    //=========== REMITTANCE =============================================
    private List<RemittanceBankListOutput> remittanceBankListOutputList;

    private List<RemittanceAddressOutput> remittanceAddressOutput;

    private GetAccountName getAccountNameOutput;

    private RemittanceMakeTransferFinishOutput makeTransferFinishOutput;

    private List<String> lstPromoCode;



    //=========== TRANSFER =============================================
    private List<CiftpBankInfo> lstCiftpBank;

    private CiftpAccountInquiryOutput ciftpAccountInfo;

    private List<CiftpMakeConfigInfo> lstMakeTransferConfig;

    private String transHash;

    private GetDetailTransactionHistoryOutput getDetailTransactionHistoryOutput;

    private String qrPayType;

    private String transferType;

    private String bakongAcctId;

    //=========== MERCHANT =============================================
    private MerchantInfo merchantInfo;



    //=========== RM =============================================
    private RmCodeOutput rmCodeOutput;

    private List<RmCodeOutput> lstRmCodeOutput;

    //=========== ADDRESS =============================================
    public List<Country> lstCountry;

    public List<Province> lstProvince;

    public List<District> lstDistrict;

    public List<Ward> lstWard;

    //PARTNER SDK
    private PartnerSdkResponse partnerSdk;

    //DIGITAL LOAN
    private Object loanOutput;
    private String t24DayNow;
    private MsLoanGetPdOutput pdOutput;
    private List<PaymentHistoryOutPut> paymentHistoryOutput;
    private RepaymentInfo repaymentInfo;
    //endregion

    //region Salary Advance
    private CustInfoOutput custInfoOutput;
    //endregion
}
