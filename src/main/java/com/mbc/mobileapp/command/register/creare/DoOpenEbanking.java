package com.mbc.mobileapp.command.register.creare;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.constant.TransactionAuthMethod;
import com.mbc.common.entity.*;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.*;
import com.mbc.common.rest.bean.RmInfo;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.ChannelEnum;
import com.mbc.mobileapp.constant.KycType;
import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DoOpenEbanking implements Command {

    private final static ObjectMapper mapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private CustRepo custRepo;

    // @Autowired
    // private ComAuthDeviceRepo comAuthDeviceRepo;

    @Autowired
    private ComAuthMthdRepo comAuthMthdRepo;

    @Autowired
    private ImIeUserRepo imIeUserRepo;

    @Autowired
    private ComOpenOnlAcctRepo comOpeningAcctRepo;

    @Autowired
    private ComRmRepo comRmRepo;

    // @Autowired
    // private ApiCustomer apiCustomer;

    @Autowired
    private ApplicationContext applicationContext;
    
//    @Autowired
//    private DoExecuteCashback doExecuteCashback;

    @Override
    public boolean execute(Context cntxt) throws Exception {

        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        Validator.Result result = Validator.Result.OK;
        RegisterCustInfo openCustomerInfo = request.getRegisterCustInfo();
        CustInfo custInfo = context.getCustomer();

        try {

            // List<Cust> custEmail = custRepo.findByCorrespondentEmail(openCustomerInfo.getEmail());
            // List<Cust> custUserId = custRepo.findByUserId(openCustomerInfo.getUsername().trim().toUpperCase());
            //
            // if (custEmail.size() > 0) {
            // result = new SimpleResult(MBCResponseCode.EMAIL_EXITSTED.getDesc(), false,
            // MBCResponseCode.EMAIL_EXITSTED.getErrorCode());
            // } else if (custUserId.size() > 0) {
            // result = new SimpleResult(MBCResponseCode.ACCOUNT_EXITSTED.getDesc(), false,
            // MBCResponseCode.ACCOUNT_EXITSTED.getErrorCode());
            // } else {
            //
            // }

            String password = request.getPinCode();
            password = MD5.MD5Password(password);
//            if (password.length() < 32) {
//                password = MD5.MD5Password(password);
//            }
            password = BCrypt.hashpw(password, BCrypt.gensalt(6));

            ImIeUser user = new ImIeUser();
            user.setName(openCustomerInfo.getCustName());
            user.setIsDelete(Constant.NO);
            user.setIsForceChangePwd(Constant.NO);
            user.setIsInactive(Constant.NO);
            user.setStatus(Constant.USER_STATUS_ACTIVE);
            user.setIsPwdNeverExpire(Constant.NO);
            user.setPasswordchangedate(new Date());
            user.setPassword(password);
            user.setPinCode(password);
            user.setLoginCount(BigDecimal.ZERO);
            imIeUserRepo.saveAndFlush(user);

            Cust cust = null;
            if (custInfo != null) {
                cust = custRepo.findById(custInfo.getId()).get();
                cust.setCorrespondentEmail(openCustomerInfo.getEmail());
                cust.setUserId(openCustomerInfo.getUsername().trim().toUpperCase());
                cust.setSrvcPcCd(Constant.PKG_SERVICE_IS_EKYC_CUSTOMER);
                cust.setSpiUsrCd(user.getCd());
                cust.setIsInactive(Constant.NO);
                cust.setIsDelete(Constant.NO);
                cust.setCreatedDateUserId(new Date());
                cust.setChannelCd(ChannelEnum.SDK_RETAIL.getCode());
                custRepo.saveAndFlush(cust);

                // ComAuthDevice comAuthDevice = new ComAuthDevice();
                // comAuthDevice.setDeviceNo(openCustomerInfo.getPhoneNumber());
                // comAuthDevice.setRegisteredBy("SYSTEM");
                // comAuthDevice.setCustId(cust.getId());
                // comAuthDevice.setSmsCount("0");
                // comAuthDevice.setIsDefault("1");
                // comAuthDevice.setDeviceType("SMS");
                // comAuthDevice.setVersion(BigDecimal.ZERO);
                // comAuthDevice.setStatus("1");
                // comAuthDevice.setRetry(BigDecimal.ZERO);
                // comAuthDeviceRepo.saveAndFlush(comAuthDevice);

                ComAuthMthd comAuthMthd = ComAuthMthd.builder().type(TransactionAuthMethod.AUTH_METHOD_SMS)
                    .createdBy(openCustomerInfo.getUsername()).custId(cust.getId()).isDefault(Constant.STATUS_1)
                    .status(Constant.STATUS_1).retry(BigDecimal.ZERO.intValue())
                    .phoneNo(openCustomerInfo.getPhoneNumber()).build();
                comAuthMthdRepo.saveAndFlush(comAuthMthd);

                ComOpenOnlAcct openAcct = (ComOpenOnlAcct) context.getVar("ComOpeningAcct");
                if (openAcct != null) {
                    openAcct.setOpenEbanking("200");
                    comOpeningAcctRepo.saveAndFlush(openAcct);
                }
                context.putVar("custId", cust.getId());

                String rmMobile = Optional.ofNullable(openCustomerInfo.getRmInfo())
                        .map(RmInfo::getRmMobile)
                        .orElse(null);

                if (!Utility.isNull(rmMobile) || !Utility.isNull(openCustomerInfo.getPartnerCode())) {
                    ComRm comRm = new ComRm();
                    comRm.setCustId(cust.getId());
                    comRm.setSrvc(request.getSrvcCd());
                    comRm.setType(Constant.RM.RM_MAIN);
                    comRm.setPartnerCode(openCustomerInfo.getPartnerCode());

                    Optional.ofNullable(openCustomerInfo.getRmInfo())
                            .ifPresent(rmInfo -> {
                                comRm.setRmCode(rmInfo.getRmCode());
                                comRm.setRmName(rmInfo.getRmName());
                                comRm.setRmPhoneNo(rmInfo.getRmMobile());
                            });
                    comRmRepo.saveAndFlush(comRm);
                }


                // if(!Utility.isNull(openCustomerInfo.getRmInfo().getRmMobile())
                // && !Utility.isNull(openCustomerInfo.getRmInfo().getRmName())) {
                // ComRm comRm = new ComRm();
                // comRm.setCustId(cust.getId());
                // comRm.setRmCode(openCustomerInfo.getRmInfo().getRmCode());
                // comRm.setRmName(openCustomerInfo.getRmInfo().getRmName());
                // comRm.setRmPhoneNo(openCustomerInfo.getRmInfo().getRmMobile());
                // comRm.setSrvc(request.getSrvcCd());
                // comRm.setType(Constant.RM.RM_MAIN);
                // comRmRepo.saveAndFlush(comRm);
                // }

                // UPDATE SECTOR CUSTOMER
                // if (cust.getCustSectorCd().equals("1891")) {
                // T24UpdateCustomer messageInput = new T24UpdateCustomer();
                // //1891 CHUA eKYC, 1890 DA eKYC
                // messageInput.setSector("1890");
                //
                // ExecuteT24Output<T24CustomerDataOutput> output_update = apiCustomer.
                // updateCustomer(messageInput, cust.getId(), request.getRequestId(), cust.getHostCifId());
                // if(Constant.CALL_MICROSERVICE_SUCCESS.equals(output_update.getStatus())) {
                // cust.setCustSectorCd("1890");
                // cust.setSrvcPcCd(Constant.PKG_SERVICE_IS_EKYC_CUSTOMER);
                // }else {
                // cust.setSrvcPcCd(Constant.PKG_SERVICE_ANONYMOUS_CUSTOMER);
                // }
                //
                // }else if(cust.getCustSectorCd().equals("1700")) {
                // cust.setSrvcPcCd(Constant.PKG_SERVICE_IS_FULL_KYC_CUSTOMER);
                // }else if(cust.getCustSectorCd().equals("1890")){
                // cust.setSrvcPcCd(Constant.PKG_SERVICE_IS_EKYC_CUSTOMER);
                // }else {
                // cust.setSrvcPcCd(Constant.PKG_SERVICE_ANONYMOUS_CUSTOMER);
                // }

                if (cust.getKycStatus().equals(KycType.FULL_KYC_TYPE.getMessage())) {
                    cust.setSrvcPcCd(Constant.PKG_SERVICE_IS_FULL_KYC_CUSTOMER);
                }
                else if (cust.getKycStatus().equals(KycType.EKYC_TYPE.getMessage())) {
                    cust.setSrvcPcCd(Constant.PKG_SERVICE_IS_EKYC_CUSTOMER);
                }
                else {
                    cust.setSrvcPcCd(Constant.PKG_SERVICE_ANONYMOUS_CUSTOMER);
                }

                custRepo.saveAndFlush(cust);
                this.preparePartnerNotify(context, cust);

//                // LUU THONG TIN EKYC
//                BindingPostCheckProcess process = applicationContext.getBean(BindingPostCheckProcess.class);
//                process.setName("BindingPostCheckProcess 1");
//                process.setData(cust, openCustomerInfo.getBioId(), request);
//                process.start();

                // // DANG KY TAI KHOAN LOYALTY
                // RegisterLoyaltyProcess registerLoyaltyProcess =
                // applicationContext.getBean(RegisterLoyaltyProcess.class);
                // registerLoyaltyProcess.setName("RegisterLoyaltyProcess");
                // registerLoyaltyProcess.setData(custInfo, request.getRequestId());
                // registerLoyaltyProcess.start();

                if (openCustomerInfo.getRmInfo() != null && !Utility.isNull(openCustomerInfo.getRmInfo().getRmMobile())
                    && Utility.isNull(openCustomerInfo.getPartnerCode())) {
                    List<Cust> lstCustRm = custRepo.findByIsDeleteAndPhoneNoAndUserIdNotNull(Constant.NO,
                        openCustomerInfo.getRmInfo().getRmMobile());
                    if (!lstCustRm.isEmpty()) {
                        Cust custRm = lstCustRm.get(0);
                        CustInfo custInfoRm = mapper.convertValue(custRm, CustInfo.class);
                        context.putVar(Constant.KeyVar.CUSTOMER_INFO_RM_INTRODUCE, custInfoRm);
                        context.putVar(Constant.KeyVar.LOYALTY_RM_POINT_EARN, Constant.SrvcCd.SRVC_OPEN_ONL_ACCT_RM);
                        
                      //CASHBACK                       
//                        CarryService carryService = new CarryService(doExecuteCashback);
//                        carryService.execute(context);
                    }
                }
            }
            else {
                request.setCreateEbank("Ebank creation failed");
                result = new SimpleResult(MBCResponseCode.OPEN_EBANKING_ERROR.getDesc(), false,
                    MBCResponseCode.OPEN_EBANKING_ERROR.getCode());
            }

        }
        catch (Exception e) {
            request.setCreateEbank("Ebank creation failed");
            AppLog.error("ERROR", e);
            result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
        }

        context.setResult(result);
        context.setRequest(request);
        return !result.isOk();
    }

    private void preparePartnerNotify(ProcessContext context, Cust cust) {
        ComPartnerNotify partnerNotify = ComPartnerNotify.builder()
                .serviceName(context.getRequest().getSrvcCd())
                .custId(cust.getHostCifId())
                .idType(cust.getIdTypType())
                .idCard(cust.getIdTypNo())
                .transactionDate(new Date())
                .detail(cust.getNm())
                .mbcTransaction(JSON.stringify(cust))
                .build();
        context.putVar("PARTNER_NOTIFY", partnerNotify);
    }

}
