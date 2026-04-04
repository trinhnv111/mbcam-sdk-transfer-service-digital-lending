package com.mbc.mobileapp.command.register;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.dto.District;
import com.mbc.common.dto.Province;
import com.mbc.common.dto.Ward;
import com.mbc.common.entity.ComAuthEkyc;
import com.mbc.common.entity.ComTokenInitSdk;
import com.mbc.common.entity.Cust;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.*;
import com.mbc.common.services.il.customerinfo.CustomerAddress;
import com.mbc.common.services.il.customerinfo.CustomerBusinessType;
import com.mbc.common.services.il.customerinfo.CustomerInfoInput;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.constant.KycType;
import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.repository.CustRepoExtend;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class DoValidateCustInfo implements Command {

    private final static ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private ComAuthEkycRepo comAuthEkycRepo;

    @Autowired
    private CustRepo custRepo;

    @Autowired
    private CustRepoExtend custRepoExtend;

    @Autowired
    private ApiCustomer apiCustomer;

    @Autowired
    private ComProvinceRepo comProvinceRepo;

    @Autowired
    private ComDistrictRepo comDistrictRepo;

    @Autowired
    private ComWardRepo comWardRepo;

    @Autowired
    private ComTokenInitSdkRepo comTokenInitSdkRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        RegisterCustInfo customerInfo = request.getRegisterCustInfo();

        try {

            result = checkInputDataCust(customerInfo, result);

            if(result.isOk()){

                ComTokenInitSdk comTokenInitSdk = comTokenInitSdkRepo.findById(request.getTId()).get();
                if(Objects.nonNull(comTokenInitSdk) && Constant.STATUS_SUCCESS.equals(comTokenInitSdk.getIsVerify())){
                    if (Objects.nonNull(comTokenInitSdk.getIdCard()) && !comTokenInitSdk.getIdCard().equals(customerInfo.getIdCardNumber())){

                        AppLog.error("[SDK Check ID CARD Validate Cust Info Register not match init SDK] requestId: "+request.getRequestId());
                        result = new SimpleResult(ResponseCode.DATA_INVALID.getDesc(), false,
                                ResponseCode.DATA_INVALID.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }
                }

                result = checkCustInfo(customerInfo, result, context);
            }


        }catch (Exception e){
            AppLog.error("[SDK Exception Validate Cust Info Register] requestId: "+request.getRequestId()+ " ,desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }

    private Validator.Result checkInputDataCust(RegisterCustInfo customerInfo, Validator.Result result){

        if(Utility.isCardNumber(customerInfo.getIdCardNumber())
                || Utility.isNull(customerInfo.getCustName())
                || !Utility.isFullNameValid(customerInfo.getCustName())
                || Utility.isNull(customerInfo.getGender())
                || Utility.isNull(customerInfo.getPhoneNumber())
                || Utility.isNull(customerInfo.getIdCardType())
                // || Utility.isNull(param.getEkycInfo().getPlaceOfIssue())
                || (customerInfo.getDob() == null)
                // || (param.getEkycInfo().getDateOfIssue() == null)
                || (customerInfo.getDateOfExpire() == null))
        {
            result = new SimpleResult(ResponseCode.DATA_INVALID.getDesc(), false,
                    ResponseCode.DATA_INVALID.getCode());
        }else{
            if(!Utility.isNull(customerInfo.getEmail())) {
                List<Cust> custEmail = custRepoExtend.findByCorrespondentEmailIgnoreCase(customerInfo.getEmail());
                if (custEmail.size() > 0) {
                    for (Cust custe : custEmail) {
                        if(Constant.NO.equals(custe.getIsDelete())) {
                            result = new SimpleResult(
                                    "The email address " + customerInfo.getEmail()
                                            + " is already in use. Please enter another email address",
                                    false, MBCResponseCode.EMAIL_EXITSTED.getCode());
                            return result;
                        }
                    }
                }
            }
        }

        return result;
    }

    private Validator.Result checkCustInfo(RegisterCustInfo customerInfo, Validator.Result result, ProcessContext context) throws Exception{

        Cust cust = null;
        CustInfo custInfo = null;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();

        if(customerInfo.isEkycSuccess() && customerInfo.getBioId() != null) {
            ComAuthEkyc comAuthEkyc = comAuthEkycRepo.findByBioId(customerInfo.getBioId());
            cust = custRepo.findById(comAuthEkyc.getCustId()).get();
            cust.setPhoneNo(cust.getPhoneNo());
            custRepo.saveAndFlush(cust);
            AppLog.info("info: " + JSON.stringify(cust));
        }else {
            CustomerInfoInput body = new CustomerInfoInput();
            body.setCustomerNationalId(customerInfo.getIdCardNumber());
            ExecuteT24Output<CustomerInfoT24> esbOutput =
                    apiCustomer.getCustomerInfo(body, null, request.getRequestId());

            if(esbOutput != null) {
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(esbOutput.getStatus())) {

                    if(KycType.BASIC_TYPE.getCode().equals(customerInfo.getKycType())) {
                        result = new SimpleResult(MBCResponseCode.OPEN_APP_REQUIRED_EKYC.getDesc(), false,
                                MBCResponseCode.OPEN_APP_REQUIRED_EKYC.getCode());
                        context.setResult(result);
                        return result;
                    }

                    CustomerInfoT24 customerInfoT24 = esbOutput.getData();
//                        cust = custRepo.findByIdTypNoAndUserIdIsNull(authenEkyc.getIdCardNumber());
                    cust = custRepo.findByIdTypNo(customerInfo.getIdCardNumber());

                    if(cust == null) {
                        cust = new Cust();
                    }

                    if(!Utility.isNull(cust.getUserId())) {
                        result = new SimpleResult(MBCResponseCode.IDENTITY_CARD_EXITSTED.getDesc(), false,
                                MBCResponseCode.IDENTITY_CARD_EXITSTED.getCode());
                        context.setResult(result);
                        return result;
                    }

                    if(Utility.isNull(customerInfoT24.getCustomerName().getVnName().trim())) {
                        cust.setNm(customerInfoT24.getCustomerName().getShortName().trim());
                    }else {
                        cust.setNm(customerInfoT24.getCustomerName().getVnName().trim());
                    }
                    cust.setIdTypNo(customerInfoT24.getPerson().getPersonalID().get(0).getIDCode());
                    cust.setHostCifId(customerInfoT24.getCustomerId());
                    cust.setPhoneNo(customerInfo.getPhoneNumber());


                    if(KycType.FULL_KYC_TYPE.name().equals(customerInfoT24.getCustKycStatus())) {

                        Date dob = DateUtil.convertStringToDate(customerInfoT24.getPerson().getDateOfBirth(),
                                DateUtil.DATE_SIMPLE_REVERSE);
                        cust.setGenderCd(customerInfoT24.getPerson().getGender());
                        cust.setDob(dob);
                        Date issueDate = DateUtil.convertStringToDate(
                                customerInfoT24.getPerson().getPersonalID().get(0).getIDIssueDate(), DateUtil.DATE_SIMPLE_REVERSE);
                        cust.setIdTypDt(issueDate);
                        Date expDate = DateUtil.convertStringToDate(
                                customerInfoT24.getPerson().getPersonalID().get(0).getIDExpiryDate(), DateUtil.DATE_SIMPLE_REVERSE);
                        cust.setIdTypExpDt(expDate);
                        cust.setIdTypPlace(customerInfoT24.getPerson().getPersonalID().get(0).getIDIssuePlace());
                        cust.setIdTypType(customerInfo.getIdCardType().toUpperCase());

                        cust.setVersion(BigDecimal.ZERO);
                        cust.setCorrespondentEmail(customerInfoT24.getContactInfo().getEmailAddress());
                        cust.setOrgUnitCd(customerInfoT24.getBranchInfo().getCode());
                        cust.setKycStatus(customerInfoT24.getCustKycStatus().toUpperCase());
                        cust.setOccupationId(Objects.nonNull(customerInfoT24.getEmploymentStatusInfo())
                                ? customerInfoT24.getEmploymentStatusInfo().get(0).getOccupation()
                                : null);

                        for (CustomerAddress address : customerInfoT24.getContactInfo().getAddress()) {
                            if ("Current".equals(address.getAddressTypeCode())) {
                                cust.setProvinceCode(address.getProvinceCode());
                                cust.setDistrictCode(address.getDistrictCode());
                                cust.setWardCode(address.getWardCode());
                                cust.setAddr1(address.getStreet());
                            }
                        }


                        cust.setIsDelete(Constant.YES);
                        cust.setIsInactive(Constant.YES);
                        for (CustomerBusinessType type : customerInfoT24.getBusinessType()) {
                            if (type.getType().equals("SECTOR")) {
                                cust.setCustSectorCd(type.getCode());
                            }
                        }
                    }else {

                        cust.setIdTypType(customerInfo.getIdCardType().toUpperCase());
                        cust.setIdTypDt(customerInfo.getDateOfIssue());
                        cust.setIdTypExpDt(customerInfo.getDateOfExpire());
                        cust.setIdTypPlace(customerInfo.getPlaceOfIssue());
                        cust.setGenderCd(customerInfo.getGender() != null ? customerInfo.getGender().toUpperCase() : null);
                        cust.setDob(customerInfo.getDob());
                        cust.setOrgUnitCd(CommonServiceConstant.BRANCH_CODE_HO);
                        cust.setVersion(BigDecimal.ZERO);
                        cust.setIsDelete(Constant.YES);
                        cust.setIsInactive(Constant.YES);
                        cust.setKycStatus(KycType.BASIC_TYPE.name());
                    }


                    custRepo.saveAndFlush(cust);
//                        context.putVar(Constant.KeyVar.CUSTOMER_INFO, custInfo);
                }
                else {
//                        cust = custRepo.findByIdTypNoAndUserIdIsNull(authenEkyc.getIdCardNumber());
                    cust = custRepo.findByIdTypNo(customerInfo.getIdCardNumber());
                    if(cust == null) {
                        cust = new Cust();
                    }

                    if(!Utility.isNull(cust.getUserId())) {
                        result = new SimpleResult(MBCResponseCode.IDENTITY_CARD_EXITSTED.getDesc(), false,
                                MBCResponseCode.IDENTITY_CARD_EXITSTED.getCode());
                        context.setResult(result);
                        return result;
                    }

                    cust.setNm(customerInfo.getCustName().toUpperCase());
                    cust.setIdTypNo(customerInfo.getIdCardNumber());
                    cust.setIdTypType(customerInfo.getIdCardType().toUpperCase());
                    cust.setIdTypDt(customerInfo.getDateOfIssue());
                    cust.setIdTypExpDt(customerInfo.getDateOfExpire());
                    cust.setIdTypPlace(customerInfo.getPlaceOfIssue());
                    cust.setGenderCd(customerInfo.getGender() != null ? customerInfo.getGender().toUpperCase() : null);
                    cust.setDob(customerInfo.getDob());
                    cust.setPhoneNo(customerInfo.getPhoneNumber());
                    cust.setOrgUnitCd(CommonServiceConstant.BRANCH_CODE_HO);
                    cust.setVersion(BigDecimal.ZERO);
                    cust.setIsDelete(Constant.YES);
                    cust.setIsInactive(Constant.YES);
                    custRepo.saveAndFlush(cust);

//                        cust.setIdTypDt(openCustomerInfo.getDateOfIssue());
//                        cust.setIdTypPlace(openCustomerInfo.getPlaceOfIssue());
//                        cust.setHostCifId(il_output.getData().getT24VersionId());
//                        cust.setCorrespondentEmail(openCustomerInfo.getEmail());

                }

//                    boolean binding = bindingEkyc(authenEkyc, cust, request.getRequestId());
//                    if(!binding) {
//                        result = new SimpleResult(MBCResponseCode.BINDING_EKYC_ERROR.getDesc(), false,
//                            MBCResponseCode.BINDING_EKYC_ERROR.getErrorCode());
//                    }

                ComAuthEkyc comAuthEkyc = comAuthEkycRepo.findByCustId(cust.getId());
                if(comAuthEkyc == null) {
                    comAuthEkyc = new ComAuthEkyc();
                    comAuthEkyc.setBioId(customerInfo.getBioId());
                    comAuthEkyc.setBioLevel(customerInfo.getBioLevel());
                    comAuthEkyc.setDeviceId(customerInfo.getDeviceId());
                    comAuthEkyc.setEkycType(customerInfo.getEkycType());
                    comAuthEkyc.setCustId(cust.getId());
                    comAuthEkyc.setCreatedBy(Constant.CREATED_BY_SYSTEM);
                    if (!Utility.isNull(cust.getId())) {
                        comAuthEkyc.setHashUserBank(Utility.sha256(cust.getId()));
                    }
                    comAuthEkyc.setSessionId(customerInfo.getSessionId());
                    comAuthEkycRepo.saveAndFlush(comAuthEkyc);

                }else if(!"0000".equals(comAuthEkyc.getBindingPCheckCode())) {
                    comAuthEkyc.setBioId(customerInfo.getBioId());
                    comAuthEkyc.setSessionId(customerInfo.getSessionId());
                    comAuthEkyc.setDeviceId(customerInfo.getDeviceId());
                    comAuthEkycRepo.saveAndFlush(comAuthEkyc);

                }else {
                    result = new SimpleResult(MBCResponseCode.INFO_EKYC_INVALID.getDesc(), false,
                            MBCResponseCode.INFO_EKYC_INVALID.getCode());
                }

            }else {
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
            }
        }
        custInfo = mapper.convertValue(cust, CustInfo.class);
        custInfo.setGender(cust.getGenderCd());
        getAddresCustomer(cust, custInfo);
        context.putVar(Constant.KeyVar.CUSTOMER_INFO, custInfo);

        return result;
    }

    private void getAddresCustomer(Cust cust, CustInfo custInfo) {
        if(!Utility.isNull(cust.getProvinceCode())) {
            Province province = comProvinceRepo.getByProvinceCode(cust.getProvinceCode());
            custInfo.setProvince(province);
        }
        if(!Utility.isNull(cust.getDistrictCode())) {
            District district = comDistrictRepo.findByDistrictCode(cust.getDistrictCode());
            custInfo.setDistrict(district);
        }
        if(!Utility.isNull(cust.getWardCode())) {
            Ward ward = comWardRepo.findByWardCode(cust.getWardCode());
            custInfo.setWard(ward);
        }
    }
}
