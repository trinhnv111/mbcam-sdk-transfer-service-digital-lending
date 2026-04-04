package com.mbc.mobileapp.command.register;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.constants.OnboardingStep;
import com.mbc.common.entity.ComAuthEkyc;
import com.mbc.common.entity.ComLogOnboarding;
import com.mbc.common.entity.Cust;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.repository.ComAuthEkycRepo;
import com.mbc.common.repository.ComLogOnboardingRepo;
import com.mbc.common.repository.CustRepo;
import com.mbc.common.services.CarryService;
import com.mbc.common.services.il.customerinfo.CustomerInfoInput;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.NonSavingAcctInput;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DoValidIdentityCard implements Command {        
    
    private final String pattern = "^[A-Z0-9]{8,12}$";

    @Autowired
    private CustRepo custRepo;

    @Autowired
    private ApiCustomer apiCustomer;
    
    @Autowired
    private ComAuthEkycRepo comAuthEkycRepo;

    @Autowired
    private ComLogOnboardingRepo comLogOnboardingRepo;

    @Autowired
    private DoGenOTPByPhone doGenOTPByPhone;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        List<String> currency = Arrays.asList("USD", "KHR");
        
        try {
            if(!request.getIdTypNo().matches(pattern)) {
                result = new SimpleResult(MBCResponseCode.IDENTITY_CARD_INVALID.getDesc(), false, 
                    MBCResponseCode.IDENTITY_CARD_INVALID.getCode());
                
            }else {
                Cust cust = custRepo.findByIdTypNo(request.getIdTypNo());   
                if (cust != null && !Utility.isNull(cust.getUserId())) {
                    result = new SimpleResult(MBCResponseCode.IDENTITY_CARD_EXITSTED.getDesc(), false,
                        MBCResponseCode.IDENTITY_CARD_EXITSTED.getCode());
                }
                else {

                    CustomerInfoInput body = new CustomerInfoInput();
                    body.setCustomerNationalId(request.getIdTypNo());
                    ExecuteT24Output<CustomerInfoT24> esbOutput =
                        apiCustomer.getCustomerInfo(body, null, request.getRequestId());

                    if (esbOutput != null) {
                        boolean eKYCSuccess = false;                                                
                        
                        if (cust != null) {
                            ComAuthEkyc comAuthEkyc = comAuthEkycRepo.findByCustId(cust.getId());               
                            if(comAuthEkyc != null) {
//                                if (comAuthEkyc.getBindingCode() != null && "0000".equals(comAuthEkyc.getBindingCode())) {
//                                    eKYCSuccess = true;
//                                }
                                response.setBioId(comAuthEkyc.getBioId());
                                response.setHashBankId(comAuthEkyc.getHashUserBank());
                                AppLog.info("info bio: " + comAuthEkyc.getBioId() + "idCardNumber: " + request.getIdTypNo());
                            }
                        }
                             
                        if (Constant.CALL_MICROSERVICE_SUCCESS.equals(esbOutput.getStatus())) {
                            
//                            //CHECK BLACK LIST CIF
//                            List<ComBlackListCif> lstBlack = comBlackListCifRepo
//                                .findByCifAndSrvcCdAndStatus(esbOutput.getData().getCustomerId(), request.getSrvcCd(), Constant.STATUS_1);
//                            if(lstBlack.size() > 0) {
//                                result = new SimpleResult(MBCResponseCode.IDENTITY_CARD_INVALID.getDesc(), false,
//                                    MBCResponseCode.IDENTITY_CARD_INVALID.getErrorCode());
//                                context.setResult(result);
//                                return !result.isOk();
//                            }
                            
                            ExecuteT24Output<List<AccountBase>> acct_output = new ExecuteT24Output<List<AccountBase>>();
                            NonSavingAcctInput inputMessage = new NonSavingAcctInput();
                            inputMessage.setCustomerId(esbOutput.getData().getCustomerId());
                            acct_output =
                                apiCustomer.getNonSavingAccountList(inputMessage, null, request.getRequestId());
                            if(Constant.CALL_MICROSERVICE_SUCCESS.equals(acct_output.getStatus())) {
                                boolean check_account = false;
                                for (AccountBase account : acct_output.getData()) {
                                    if(Constant.ACCT_STATUS_ACTIVE.equals(account.getAcctnStatus())) {
                                        check_account = true;
                                        break;
                                    }
                                }
                                
                                if(check_account) {
                                    currency = Collections.emptyList();
                                    result = new SimpleResult(MBCResponseCode.NON_SAVING_ACCOUNT_ALREADY_EXISTS.getDesc(), false,
                                        MBCResponseCode.NON_SAVING_ACCOUNT_ALREADY_EXISTS.getCode());
                                }else {
                                    result = new SimpleResult(MBCResponseCode.OPEN_APP_REQUIRED_EKYC.getDesc(), false,
                                        MBCResponseCode.OPEN_APP_REQUIRED_EKYC.getCode());
                                }
                                          
                            }else if("400".equals(acct_output.getStatus())) {
                                result = new SimpleResult(MBCResponseCode.OPEN_APP_REQUIRED_EKYC.getDesc(), false,
                                    MBCResponseCode.OPEN_APP_REQUIRED_EKYC.getCode());
                            } else {
                                
                                String response_code = acct_output.getErrorInfo().getErrorCode();
                                String response_msg = acct_output.getErrorInfo().getErrorDesc();
                                String response_dtl = acct_output.getErrorInfo().getErrorDetail();
                                result = new SimpleResult(response_msg + " - " + response_dtl, false, response_code);
//                                
//                                result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false,
//                                    ResponseCode.COMMON_FAIL.getCode());                              
                            }
                            response.setEkycSuccess(eKYCSuccess);
                            response.setLstCurrency(currency);
                        }
                        else if ("400".equals(esbOutput.getStatus())
                            && "4140".equals(esbOutput.getErrorInfo().getErrorCode())) {
                            result = new SimpleResult(MBCResponseCode.IDENTITY_CARD_NOT_EXIST.getDesc(), false,
                                MBCResponseCode.IDENTITY_CARD_NOT_EXIST.getCode());
                            response.setEkycSuccess(eKYCSuccess);
                            response.setLstCurrency(currency);
                        }
                        else {
                            result = new SimpleResult(
                                esbOutput.getErrorInfo().getErrorDesc() + " - " + esbOutput.getErrorInfo().getErrorDetail(),
                                false, esbOutput.getErrorInfo().getErrorCode());
                        }
                    }
                    else {
                        result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                            ResponseCode.TRANSACTION_FAIL.getCode());
                    }
                }
            }
        }
        catch (Exception e) {
            AppLog.error("ERROR", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }

        logValidateIdCard(request, result);

        List<String> responseCode = Arrays.asList("GW316", "GW306", "GW339");
        if(responseCode.indexOf(result.getResponseCode()) != -1){
            CarryService carryService = new CarryService(doGenOTPByPhone);
            carryService.execute(context);
            result = context.getResult();
        }


        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

    private void logValidateIdCard(CommonServiceRequest request, Validator.Result result){
        String responseCode = "GW316,GW306,GW339";

        ComLogOnboarding comLogOnboarding = ComLogOnboarding.builder()
                .deviceId(request.getDeviceIdCommon())
                .routeKey(request.getRouteKey())
                .content(request.getIdTypNo()+","+request.getIdCardType())
                .idCardNumber(request.getIdTypNo())
                .idCardType(request.getIdCardType())
                .step(OnboardingStep.STEP_CHECK_IDENTITY)
                .responseCode(responseCode.indexOf(result.getResponseCode()) != -1 ? "000" : result.getResponseCode())
                .responseDesc(responseCode.indexOf(result.getResponseCode()) != -1 ? "OK" : result.getMessage())
                .timeProcess(new BigDecimal(Calendar.getInstance().getTimeInMillis() - request.getStartRequest().getTime()))
                .requestTime(request.getStartRequest())
                .requestId(request.getRequestId())
                .channel(request.getDigitalChannel())
                .build();
        comLogOnboardingRepo.saveAndFlush(comLogOnboarding);
    }

}
