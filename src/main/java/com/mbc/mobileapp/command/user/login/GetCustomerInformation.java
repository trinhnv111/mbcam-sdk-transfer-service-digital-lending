
package com.mbc.mobileapp.command.user.login;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.*;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.*;
import com.mbc.common.services.il.customerinfo.CustomerInfoInput;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class GetCustomerInformation implements Command {

    // @Autowired
    // private CustCustomRepo custCustomRepo;

    @Autowired
    private ComAuthEkycRepo comAuthEkycRepo;

    @Autowired
    private ComMobileUtilityRepo mobileUtilityRepository;

    @Autowired
    private AcctRepo acctRepo;

    @Autowired
    private ApiCustomer apiCustomer;

    @Autowired
    private CustRepo custRepo;
    
    @Autowired
    private ImIeUserRepo imIeUserRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

//        CustInfo customer = custCustomRepo.getCustByUserId(request.getUserId(), request.getDigitalChannel());
        CustInfo customer = context.getCustomer();
        Cust cust = null;
              
        try {
            if (customer == null) {
                result = new SimpleResult(ResponseCode.CUSTOMER_INVALID.getDesc(), false,
                    ResponseCode.CUSTOMER_INVALID.getCode());
            }
            else if (Constant.MB_CUSTOMER_STATE_NOTACTIVE == customer.getState()) {
                result = new SimpleResult(ResponseCode.CUSTOMER_INACTIVE.getDesc(), false,
                    ResponseCode.CUSTOMER_INACTIVE.getCode());
            }
            else {
                // lay danh sach tai khoan thanh toan cua khach hang
                // HashMap<String, Acct> acct_map =
                // acctCustomRepo.getAccountByCustId(customer.getHostCifId(), Constant.ACCOUNT_TYPE_CURRENT_ACCOUNT);
                // customer.setAcctList(acct_map);

                //
                // Account defaultAcct = DefaultAccountDAO.getDefaultAccount(customer.getId());
                // if (defaultAcct != null) {
                // customer.setDefaultAccount(acct_map.get(defaultAcct.getAcctNo()));
                // }
                //
                // // lay danh sách tai khoan tiet kiem cua khach hangss
                // HashMap<String, Acct> saving_acct_map = AccountDAO.getAccountListByCustIdAndType(customer.getId(),
                // new String[] { Constant.ACCOUNTTYPE_SAVINGSATBRANCHES_CODE_CONSTANT,
                // Constant.ACCOUNTTYPE_ONLINESAVINGSACCOUNT_CODE_CONSTANT });
                // customer.setSaving_acct_list(saving_acct_map);
                //
                // // sms count
                // SysParam maxVerifySmsOtp = DataCachedCollection
                // .getSysParam(SystemParameterConstants.MAX_VERIFY_SMS_OTP);
                // Calendar smsVerifyExpireTime = Calendar.getInstance();
                // smsVerifyExpireTime.setTime(DateUtil.convertStringToDate(DataCachedCollection
                // .getSysParam(SystemParameterConstants.SMS_VERIFY_EXPIRE_TIME).getValue(),
                // DateUtil.DATETIME_WITH_SLASH));
                // if (Calendar.getInstance().getTimeInMillis() >= smsVerifyExpireTime.getTimeInMillis()) {
                // customer.setSmsCount(String.valueOf(Constant.STATE_OFF));
                // } else if (!Utility.isNull(auth_device.getSmsCount())) {
                // smsCount = Integer.valueOf(maxVerifySmsOtp.getValue())
                // - Integer.valueOf(auth_device.getSmsCount());
                // customer.setSmsCount(String.valueOf(smsCount > 0 ? smsCount : Constant.STATE_OFF));
                // } else {
                // customer.setSmsCount(maxVerifySmsOtp.getValue());
                // }
                // }
                // }
                // break;
                // }
                // if (Constant.VTAP_STATUS_CODE_REGISTERED.equals(auth_device.getStatus())) {
                // customer.setIsSoftToken(Constant.VTAP_STATUS_REGISTERED);
                // }
                // }
                // if (Utility.isNull(customer.getIsSoftToken())) {
                // customer.setIsSoftToken(Constant.VTAP_STATUS_UNREGISTERED);
                // }
                // } else {
                // customer.setIsSoftToken(Constant.VTAP_STATUS_UNREGISTERED);
                // }
                //
                //
                //
                // if (Constant.NO.equals(Constant.IS_ONLY_MB_CUST_DIGITAL_OTP)) {
                // customer.setIsAcceptDigitalOTP(Constant.YES);
                // } else if (Constant.YES.equals(Constant.IS_ONLY_MB_CUST_DIGITAL_OTP)
                // && Constant.YES.equals(customer.getIsMBCust())) {
                // customer.setIsAcceptDigitalOTP(Constant.YES);
                // } else {
                // customer.setIsAcceptDigitalOTP(Constant.NO);
                // }

                // Lay thoi gian dang nhap gan nhat
                // ImIeLoginHist loginHist = ImIeLoginHistDAO.getLastLoginByUserCode(customer.getSpiUsrCd());
                // Date lastLogin = loginHist == null ? new Date() : loginHist.getLoginDt();
                // customer.setLastLogin(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastLogin));
                //
                // // Lay session timeout
                // customer.setMaxInactiveInterval(String.valueOf(DataCachedCollection.getSysConfig().getSessionTimeout()));
                //
                // // Lay thong tin sector
                // HashMap sectorMap = new HashMap();
                // SysParam privateSector = DataCachedCollection.getSysParam(SystemParameterConstants.PRIVATE_SECTOR);
                // sectorMap.put(privateSector.getNm(), privateSector.getValue());
                // privateSector = DataCachedCollection.getSysParam(SystemParameterConstants.PRIORITY_SECTOR);
                // sectorMap.put(privateSector.getNm(), privateSector.getValue());
                // customer.setSectorDetail(sectorMap);
                // // check KH can doi qua ngay de update han muc
                // if (Constant.SERVICE_PACKAGE_EBANKING_KYC_LV2.equalsIgnoreCase(customer.getSrvcPcCd())) {
                // ComSrvcPc service_pck = DataCachedCollection.getServicePackage(customer.getSrvcPcCd());
                // if (service_pck != null && LimitDAO.isLimitNotSet(customer.getId(), service_pck.getLimitPackageFk())) {
                // customer.setIsNeedUpdateLimit(Constant.YES);
                // }
                // }
                
                CustomerInfoInput input = new CustomerInfoInput();
                input.setCustomerNationalId(customer.getIdTypNo());
                ExecuteT24Output<CustomerInfoT24> custT24 =
                    apiCustomer.getCustomerInfo(input, null, request.getRequestId());
                if(Constant.CALL_MICROSERVICE_SUCCESS.equals(custT24.getStatus())) {
                    CustomerInfoT24 customerInfoT24 = custT24.getData();
                    cust = custRepo.findById(customer.getId()).get();
                    if(Utility.isNull(customerInfoT24.getCustomerName().getVnName().trim())) {
                        cust.setNm(customerInfoT24.getCustomerName().getShortName().trim());
                    }else {
                        cust.setNm(customerInfoT24.getCustomerName().getVnName().trim());
                    }
                    if(Objects.isNull(cust.getDob())) {
                        Date dob = DateUtil.convertStringToDate(customerInfoT24.getPerson().getDateOfBirth(),
                            DateUtil.DATE_SIMPLE_REVERSE);
                        cust.setDob(dob);
                    }
                    if(Objects.isNull(customerInfoT24.getCustKycStatus()) || "UNKNOW".equals(customerInfoT24.getCustKycStatus())) {
                        cust.setKycStatus(Constant.CUSTOMER_KYC_STATUS_BASIC);
                        customer.setKycStatus(Constant.CUSTOMER_KYC_STATUS_BASIC);
                    }else {
                        cust.setKycStatus(customerInfoT24.getCustKycStatus().toUpperCase());  
                        customer.setKycStatus(customerInfoT24.getCustKycStatus().toUpperCase());
                    }
                    //update customer-idTypType record
                    cust.setIdTypType(customerInfoT24.getPerson().getPersonalID().get(0).getIDType());
                    
//                    response.setNationalId(customerInfoT24.getPerson().getNationality());
                    customer.setNationalId(customerInfoT24.getPerson().getNationality());
                    customer.setResident(customerInfoT24.getPerson().getResidence());
                    customer.setIdTypType(customerInfoT24.getPerson().getPersonalID().get(0).getIDType());
                    custRepo.saveAndFlush(cust);
                }
                
                
                String deviceId = request.getDeviceIdCommon();
                ComMobileUtility mobileUtility = mobileUtilityRepository.findByDeviceIdAndCustId(deviceId, customer.getId());
                if (mobileUtility != null) {
                    customer.setFingerPrint(mobileUtility.getFingerPrint());
                }
                
                List<Acct> acctList = acctRepo.findByHostCustId(customer.getHostCifId());
                if (acctList != null && !acctList.isEmpty()) {
                    customer.setViaPhone(acctList.get(0).getIsInq());
                }
                
                ComAuthEkyc comAuthEkyc = comAuthEkycRepo.findByCustId(customer.getId());
                if(comAuthEkyc != null) {                   
                    if( (comAuthEkyc.getBindingPCheckCode() != null && comAuthEkyc.getBindingPCheckCode().equals("0000")) 
                        || (comAuthEkyc.getBindingCode() != null && comAuthEkyc.getBindingCode().equals("0000")) ) {
                        customer.setEkycSuccess(true);
                    }else {
                        customer.setEkycSuccess(false);
                    }
                    customer.setBioId(comAuthEkyc.getBioId());
                    customer.setHashBankId(comAuthEkyc.getHashUserBank());
                    
                }else {
                    customer.setEkycSuccess(false);
                }
                                
                //check onboard basic
                if(customer.getCustSectorCd().equals("1890") 
                    && !customer.isEkycSuccess() 
                    && DateCompareUtil.daysDiff(customer.getCreatedDateUserId(), new Date()) > 7) {
                    ImIeUser user = imIeUserRepo.findByCd(cust.getSpiUsrCd());
                    user.setStatus(Constant.USER_STATUS_WAIT_EKYC_ONBOARD_BASIC);
                    imIeUserRepo.saveAndFlush(user);
                    customer.setImUserStatus(Constant.USER_STATUS_WAIT_EKYC_ONBOARD_BASIC);
                    
                }
                
                context.setCustomer(customer);
            }
        }
        catch (Exception e) {
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
            AppLog.error(e);
        }
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

}
