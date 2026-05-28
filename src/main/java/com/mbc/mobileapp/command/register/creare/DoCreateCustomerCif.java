package com.mbc.mobileapp.command.register.creare;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.api.ApiCustomer;
import com.mbc.common.api.models.customer.*;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.ComOpenOnlAcct;
import com.mbc.common.entity.ComPartnerNotify;
import com.mbc.common.entity.Cust;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComOpenOnlAcctRepo;
import com.mbc.common.repository.CustRepo;
import com.mbc.common.services.il.customerinfo.*;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.register.CustEmail;
import com.mbc.mobileapp.api.model.register.CustName;
import com.mbc.mobileapp.api.model.register.CustShortName;
import com.mbc.mobileapp.api.model.register.OnboardingUpdateCusInfo;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.constant.KycType;
import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DoCreateCustomerCif implements Command {

    private final static ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private CustRepo custRepo;

    @Autowired
    private ComOpenOnlAcctRepo comOpeningAcctRepo;

    @Autowired
    private ApiCustomer apiCustomer;

//    @Autowired
//    private ComAuthEkycRepo comAuthEkycRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {

        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        Validator.Result result = Validator.Result.OK;
        RegisterCustInfo openCustomerInfo = request.getRegisterCustInfo();
        Cust cust = null;
        String kycStatus = KycType.BASIC_TYPE.getMessage();

        try {

            List<Cust> lstCustPhoneNoAndTypNo = custRepo.findByIdTypNoAndPhoneNoAndUserIdIsNull(
                    openCustomerInfo.getIdCardNumber(), openCustomerInfo.getPhoneNumber());

            if (lstCustPhoneNoAndTypNo == null || lstCustPhoneNoAndTypNo.isEmpty()) {
                result = new SimpleResult(MBCResponseCode.INFO_REGISTER_EBANKING_INVALID.getDesc(), false,
                        MBCResponseCode.INFO_REGISTER_EBANKING_INVALID.getCode());
            } else if (lstCustPhoneNoAndTypNo.size() == 1) {

                cust = lstCustPhoneNoAndTypNo.get(0);
//                ComAuthEkyc ekyc = comAuthEkycRepo.findByCustId(cust.getId());
//                if (!"0000".equals(ekyc.getBindingCode())) {
//                    result = new SimpleResult(MBCResponseCode.CUSTOMER_EKYC_NOT_SUCCESS.getDesc(), false,
//                        MBCResponseCode.CUSTOMER_EKYC_NOT_SUCCESS.getErrorCode());
//                    context.setResult(result);
//                    return !result.isOk();
//                }

                String response_code = "";
                String response_msg = "";
                String response_dtl = "";
                String response_status = "";

                // KIEM TRA KH DA TON TAI HAY CHUA?
                CustomerInfoInput input = new CustomerInfoInput();
                input.setCustomerNationalId(openCustomerInfo.getIdCardNumber());

                ExecuteT24Output<CustomerInfoT24> custT24 =
                        apiCustomer.getCustomerInfo(input, null, request.getRequestId());
                if ("4140".equals(custT24.getErrorInfo().getErrorCode())) {

                    if (KycType.EKYC_TYPE.getCode().equals(openCustomerInfo.getKycType())) {
                        kycStatus = KycType.EKYC_TYPE.getMessage();
                    } else {
                        kycStatus = KycType.BASIC_TYPE.getMessage();
                    }

                    T24CustomerInfo messageInput = setMessageInput(openCustomerInfo, kycStatus, request.getPartnerSdk());
                    ExecuteT24Output<T24CustomerDataOutput> il_output =
                            apiCustomer.createCustomer(messageInput, null, request.getRequestId());

                    if (il_output != null) {
                        response_status = il_output.getStatus();
                        if (Constant.CALL_MICROSERVICE_SUCCESS.equals(response_status)) {

                            cust.setGenderCd(openCustomerInfo.getGender());
                            cust.setDob(openCustomerInfo.getDob());
                            cust.setNm(openCustomerInfo.getCustName());
                            cust.setPhoneNo(openCustomerInfo.getPhoneNumber());
                            cust.setIdTypNo(openCustomerInfo.getIdCardNumber());
                            cust.setIdTypDt(openCustomerInfo.getDateOfIssue());
                            cust.setIdTypPlace(openCustomerInfo.getPlaceOfIssue());
                            cust.setIdTypType(openCustomerInfo.getIdCardType().toUpperCase());
                            cust.setHostCifId(il_output.getData().getT24VersionId());
                            cust.setVersion(BigDecimal.ZERO);
                            cust.setKycStatus(messageInput.getCustKycStatus());
                            cust.setCorrespondentEmail(openCustomerInfo.getEmail());
                            cust.setIsDelete(Constant.YES);
                            cust.setIsInactive(Constant.YES);
                            cust.setCustSectorCd(String.valueOf(messageInput.getBusinessType().getType()));
                            cust.setAddr1(openCustomerInfo.getAddress());

                            cust.setProvinceCode(openCustomerInfo.getProvinceCode());
                            cust.setDistrictCode(openCustomerInfo.getDistrictCode());
                            cust.setWardCode(openCustomerInfo.getWardCode());

                            cust.setOccupationId(openCustomerInfo.getOccupationId());
                            cust.setOccupationTitle(openCustomerInfo.getOccupationTitle());
                            cust.setNationalId(openCustomerInfo.getNationalId());

                            custRepo.saveAndFlush(cust);
                            CustInfo custInfo = mapper.convertValue(cust, CustInfo.class);
                            custInfo.setGender(cust.getGenderCd());
                            context.setCustomer(custInfo);

                            ComOpenOnlAcct ComOpeningAcct =
                                    saveOpenAcct(openCustomerInfo, request, response_status, null, null);
                            context.putVar("ComOpeningAcct", ComOpeningAcct);

                        } else {
                            response_code = il_output.getErrorInfo().getErrorCode();
                            response_msg = il_output.getErrorInfo().getErrorDesc();
                            response_dtl = il_output.getErrorInfo().getErrorDetail();

                            result = new SimpleResult(response_msg + " - " + response_dtl, false, response_code);
                            saveOpenAcct(openCustomerInfo, request, response_status, null, null);

                            request.setCreateCif("Create cif: " + response_msg + "-" + response_dtl);
                        }
                    } else {
                        result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                                ResponseCode.TRANSACTION_FAIL.getCode());
                    }
                } else if (Constant.CALL_MICROSERVICE_SUCCESS.equals(custT24.getStatus())) {
                    CustomerInfoT24 customerInfoT24 = custT24.getData();

                    if (KycType.BASIC_TYPE.getMessage().equals(customerInfoT24.getCustKycStatus())) {

                        if (cust.getHostCifId().equals(customerInfoT24.getCustomerId())) {

                            T24UpdateCustomer messageInput = updateCustomerInfo(openCustomerInfo);
                            ExecuteT24Output<T24CustomerDataOutput> output_update = apiCustomer.
                                    updateCustomer(messageInput, cust.getId(), request.getRequestId(), cust.getHostCifId());

                            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output_update.getStatus())) {

//                                if (Utility.isNull(customerInfoT24.getCustomerName().getVnName().trim())) {
//                                    cust.setNm(customerInfoT24.getCustomerName().getShortName().trim());
//                                }
//                                else {
//                                    cust.setNm(customerInfoT24.getCustomerName().getVnName().trim());
//                                }
                                cust.setNm(openCustomerInfo.getCustName());
                                cust.setIdTypNo(customerInfoT24.getPerson().getPersonalID().get(0).getIDCode());
                                cust.setHostCifId(customerInfoT24.getCustomerId());
                                for (CustomerBusinessType type : customerInfoT24.getBusinessType()) {
                                    if (type.getType().equals("SECTOR")) {
                                        cust.setCustSectorCd(type.getCode());
                                    }
                                }

                                cust.setGenderCd(openCustomerInfo.getGender());
                                cust.setDob(openCustomerInfo.getDob());
                                cust.setPhoneNo(openCustomerInfo.getPhoneNumber());
                                cust.setIdTypDt(openCustomerInfo.getDateOfIssue());
                                cust.setIdTypPlace(openCustomerInfo.getPlaceOfIssue());
                                cust.setIdTypType(openCustomerInfo.getIdCardType().toUpperCase());

                                cust.setVersion(BigDecimal.ZERO);
                                cust.setKycStatus(messageInput.getKycStatus());
                                cust.setCorrespondentEmail(openCustomerInfo.getEmail());
                                cust.setIsDelete(Constant.YES);
                                cust.setIsInactive(Constant.YES);

                                cust.setAddr1(openCustomerInfo.getAddress());
                                cust.setProvinceCode(openCustomerInfo.getProvinceCode());
                                cust.setDistrictCode(openCustomerInfo.getDistrictCode());
                                cust.setWardCode(openCustomerInfo.getWardCode());
                                cust.setOccupationId(openCustomerInfo.getOccupationId());
                                cust.setOccupationTitle(openCustomerInfo.getOccupationTitle());

                                custRepo.saveAndFlush(cust);

                            } else {
                                response_code = output_update.getErrorInfo().getErrorCode();
                                response_msg = output_update.getErrorInfo().getErrorDesc();
                                response_dtl = output_update.getErrorInfo().getErrorDetail();

                                result = new SimpleResult(response_msg + " - " + response_dtl, false, response_code);
                            }

                        } else {
                            result = new SimpleResult(MBCResponseCode.INFO_REGISTER_EBANKING_INVALID.getDesc(), false,
                                    MBCResponseCode.INFO_REGISTER_EBANKING_INVALID.getCode());
                        }

                    } else {
                        cust.setGenderCd(customerInfoT24.getPerson().getGender().equals("0")
                                || customerInfoT24.getPerson().getGender().equals("FEMALE") ? CommonServiceConstant.GENDER_FEMALE
                                : CommonServiceConstant.GENDER_MALE);
                        Date dob = DateUtil.convertStringToDate(customerInfoT24.getPerson().getDateOfBirth(),
                                DateUtil.DATE_SIMPLE_REVERSE);
                        cust.setDob(dob);
                        if (Utility.isNull(customerInfoT24.getCustomerName().getVnName().trim())) {
                            cust.setNm(customerInfoT24.getCustomerName().getShortName().trim());
                        } else {
                            cust.setNm(customerInfoT24.getCustomerName().getVnName().trim());
                        }
                        // cust.setPhoneNo(customerInfoT24.getContactInfo().getPhone().size() > 0 ?
                        // customerInfoT24.getContactInfo().getPhone().get(0).getPhoneNo() : null);
                        cust.setPhoneNo(openCustomerInfo.getPhoneNumber());
                        cust.setIdTypNo(customerInfoT24.getPerson().getPersonalID().get(0).getIDCode());
                        Date issueDate = DateUtil.convertStringToDate(
                                customerInfoT24.getPerson().getPersonalID().get(0).getIDIssueDate(),
                                DateUtil.DATE_SIMPLE_REVERSE);
                        cust.setIdTypDt(issueDate);
                        cust.setIdTypPlace(customerInfoT24.getPerson().getPersonalID().get(0).getIDIssuePlace());
                        cust.setIdTypType(openCustomerInfo.getIdCardType().toUpperCase());
                        cust.setHostCifId(customerInfoT24.getCustomerId());
                        cust.setVersion(BigDecimal.ZERO);
                        cust.setOrgUnitCd(customerInfoT24.getBranchInfo().getCode());
                        // cust.setCorrespondentEmail(openCustomerInfo.getEmail());
                        cust.setIsDelete(Constant.NO);
                        cust.setIsInactive(Constant.YES);
                        cust.setAddr1(openCustomerInfo.getAddress());

                        cust.setOccupationId(openCustomerInfo.getOccupationId());
                        cust.setOccupationTitle(openCustomerInfo.getOccupationTitle());

                        for (CustomerBusinessType type : customerInfoT24.getBusinessType()) {
                            if (type.getType().equals("SECTOR")) {
                                cust.setCustSectorCd(type.getCode());
                            }
                        }

                        for (CustomerAddress address : customerInfoT24.getContactInfo().getAddress()) {
                            if ("Current".equals(address.getAddressTypeCode())) {
                                cust.setProvinceCode(address.getProvinceCode());
                                cust.setDistrictCode(address.getDistrictCode());
                                cust.setWardCode(address.getWardCode());
//                                cust.setAddr1(address.getStreet());

                            }
                        }

                        custRepo.saveAndFlush(cust);
                    }


                    CustInfo custInfo = mapper.convertValue(cust, CustInfo.class);
                    custInfo.setGender(cust.getGenderCd());
                    context.setCustomer(custInfo);

                    ComOpenOnlAcct ComOpeningAcct =
                            saveOpenAcct(openCustomerInfo, request, response_code, null, null);
                    context.putVar("ComOpeningAcct", ComOpeningAcct);

                    //update occupation t24
//                    CusOccupation cusOccupation = CusOccupation.builder().occupation(openCustomerInfo.getOccupationId()).build();
//                    List<CusOccupation> lstOccupation = new ArrayList<CusOccupation>();
//                    lstOccupation.add(cusOccupation);                           

//                    T24UpdateCustomer messageInput = new T24UpdateCustomer();
//                    messageInput.setEmployeeStatusList(lstOccupation);
//                    
//                    ExecuteT24Output<T24CustomerDataOutput> output = apiCustomer.
//                        updateCustomer(messageInput, cust.getId(), request.getRequestId(), cust.getHostCifId());


                    // if (cust.getHostCifId() != null) {
                    //
                    // }
                    // else {
                    // result = new SimpleResult(MBCResponseCode.INFO_REGISTER_EBANKING_INVALID.getDesc(), false,
                    // MBCResponseCode.INFO_REGISTER_EBANKING_INVALID.getErrorCode());
                    // }

                }
            } else {
                result = new SimpleResult(MBCResponseCode.OPEN_EBANKING_ERROR.getDesc(), false,
                        MBCResponseCode.OPEN_EBANKING_ERROR.getCode());
            }
        } catch (Exception e) {
            AppLog.error("ERROR", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        context.setRequest(request);
        return !result.isOk();
    }

    private String createMnemonic() {
        StringBuilder mnemonic = new StringBuilder("");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String date = LocalDate.now().format(formatter);
        RandomString firstString = new RandomString(1, new SecureRandom(), RandomString.upper);
        RandomString lastString = new RandomString(2, new SecureRandom(), RandomString.digits);
        mnemonic.append(firstString.nextString());
        mnemonic.append(date);
        mnemonic.append(firstString.nextString());
        mnemonic.append(lastString.nextString());

        return mnemonic.toString();
    }

    private T24CustomerInfo setMessageInput(RegisterCustInfo info, String kycStatus, String partner) {

        T24CustomerInfo custInfo = new T24CustomerInfo();
        custInfo.setMnemonic(createMnemonic());
        custInfo.setGender(info.getGender().toUpperCase());
        custInfo.setTarget("30");
        custInfo.setNationality(CommonServiceConstant.ID_CARD_DOC_NAME_T24_PASSPORT.equals(info.getIdCardType().toUpperCase())
                ? info.getNationalId()
                : "KH");
        custInfo.setCustomerStatus("1001");
        custInfo.setIndustry("119");
        custInfo.setLanguage("1");
        custInfo.setMaritalStatus("");
        custInfo.setLastName(info.getLastName());
        custInfo.setFirstName(info.getFirstName());
        custInfo.setTitle(info.getGender().toUpperCase().equals("MALE") ? "Mr" : "Ms");
        custInfo.setResidence(CommonServiceConstant.ID_CARD_DOC_NAME_T24_PASSPORT.equals(info.getIdCardType().toUpperCase())
                ? info.getNationalId()
                : "KH");
        custInfo.setNoOfDependents("1");
        custInfo.setAccountOfficer("1");
        custInfo.setBirthOfDate(DateFormatUtils.format(info.getDob(), DateUtil.DATE_SIMPLE_REVERSE));
        custInfo.setBranchCode(CommonServiceConstant.BRANCH_CODE_HO);
        custInfo.setShortName(info.getLastName());
        custInfo.setCamFullName(info.getCustName().toUpperCase());
        // custInfo.setGbFullName(info.getCustName().toUpperCase());
        custInfo.setEmailAddress(info.getEmail());
        custInfo.setFaxNumber(info.getPhoneNumber());
        custInfo.setChannel(CommonServiceConstant.CHANNEL_MOBILE_RETAIL);

        CustomerOccupation cusOccupation = CustomerOccupation.builder().occupation(info.getOccupationId()).build();
        List<CustomerOccupation> lstOccupation = new ArrayList<CustomerOccupation>();
        lstOccupation.add(cusOccupation);
        custInfo.setEmploymentStatusList(lstOccupation);

        // 1891 CHUA eKYC, 1890 DA eKYC
        BusinessType businessType = new BusinessType();
        businessType.setType(1890);
        custInfo.setBusinessType(businessType);
        custInfo.setCustKycStatus(kycStatus);

        CusAddress cusAddress = new CusAddress();
        cusAddress.setCountry("CAMBODIA");
        cusAddress.setFullAddress(info.getAddress());
        cusAddress.setStreet(info.getAddress());
//        cusAddress.setStreet(info.getStreet());
        cusAddress.setTown(info.getProvinceCode());
        cusAddress.setDistrict(info.getDistrictCode());
        cusAddress.setWard(info.getWardCode());
        custInfo.setCusAddress(cusAddress);

        CusPhone cusPhone = new CusPhone();
        cusPhone.setPhoneNo(info.getPhoneNumber());
        custInfo.setCusPhone(cusPhone);

        CusRelation cusRelation = new CusRelation();
        cusRelation.setRelationCode(null);
        cusRelation.setRelCustomer(null);
        custInfo.setCusRelation(cusRelation);

        PersonalId personalId = new PersonalId();
        personalId.setNationalId(info.getIdCardNumber());
        personalId.setNaIdType(CommonServiceConstant.ID_CARD_DOC_NAME_T24_PASSPORT.equals(info.getIdCardType().toUpperCase())
                ? CommonServiceConstant.ID_CARD_DOC_NAME_T24_PASSPORT
                : CommonServiceConstant.ID_CARD_DOC_NAME_T24_ID_CARD);
        personalId.setNaIdIssPlc(info.getPlaceOfIssue());
        personalId.setNaIdIssDate(
                info.getDateOfIssue() != null ? DateFormatUtils.format(info.getDateOfIssue(), DateUtil.DATE_SIMPLE_REVERSE)
                        : null);
        personalId.setNaIdExpDate(info.getDateOfExpire() != null
                ? DateFormatUtils.format(info.getDateOfExpire(), DateUtil.DATE_SIMPLE_REVERSE)
                : null);
        custInfo.setPersonalId(personalId);

        return custInfo;
    }

    private OnboardingUpdateCusInfo updateCustomerInfo(RegisterCustInfo info) {

        CusOccupation cusOccupation = CusOccupation.builder().occupation(info.getOccupationId()).build();
        List<CusOccupation> lstCusOccupations = new ArrayList<CusOccupation>();
        lstCusOccupations.add(cusOccupation);


        CustEmail custEmail = CustEmail.builder().email(info.getEmail()).build();
        List<CustEmail> lstEmails = new ArrayList<CustEmail>();
        lstEmails.add(custEmail);

        CustShortName custShortName = CustShortName.builder().shortName(info.getLastName()).build();
        List<CustShortName> lstCustShortNames = new ArrayList<CustShortName>();
        lstCustShortNames.add(custShortName);

        CustName custName = CustName.builder().name1(info.getCustName()).build();
        List<CustName> lstCustNames = new ArrayList<CustName>();
        lstCustNames.add(custName);

        return OnboardingUpdateCusInfo.builder()
                .custFullName(info.getCustName())
                .lstCustShortName(lstCustShortNames)
                .lstCustName(lstCustNames)
                .issueDate(info.getDateOfIssue() != null ? DateFormatUtils.format(info.getDateOfIssue(), DateUtil.DATE_SIMPLE_REVERSE)
                        : null)
                .dateOfExpire(info.getDateOfExpire() != null
                        ? DateFormatUtils.format(info.getDateOfExpire(), DateUtil.DATE_SIMPLE_REVERSE)
                        : null)
                .placeOfIssue(info.getPlaceOfIssue())
                .dob(DateFormatUtils.format(info.getDob(), DateUtil.DATE_SIMPLE_REVERSE))
                .gender(info.getGender().toUpperCase())
                .kycStatus(KycType.EKYC_TYPE.getMessage())
                .street(info.getStreet())
                .ward(info.getWardCode())
                .district(info.getDistrictCode())
                .town(info.getProvinceCode())
                .phoneNumber(info.getPhoneNumber())
                .employeeStatusList(lstCusOccupations)
                .listEmail(lstEmails)
                .nationality(CommonServiceConstant.ID_CARD_DOC_NAME_T24_PASSPORT.equals(info.getIdCardType().toUpperCase())
                        ? info.getNationalId()
                        : "KH")
                .build();


    }

    private ComOpenOnlAcct saveOpenAcct(RegisterCustInfo info, CommonServiceRequest request, String openCif, String openAcct,
                                        String openEbanking) throws Exception {
        ComOpenOnlAcct comOpeningAcct = new ComOpenOnlAcct();
        comOpeningAcct.setAddress(info.getAddress());
        comOpeningAcct.setName(info.getCustName());
        comOpeningAcct.setCreatedBy(info.getCustName());
        comOpeningAcct.setIdCardNumber(info.getIdCardNumber());
        comOpeningAcct.setIdCardType(info.getIdCardType());
        comOpeningAcct.setPhoneNumber(info.getPhoneNumber());
        comOpeningAcct.setOpenCif(openCif);
        comOpeningAcct.setOpenAcct(openAcct);
        comOpeningAcct.setOpenEbanking(openEbanking);
        comOpeningAcct.setUserId(info.getUsername().trim().toUpperCase());
        comOpeningAcct.setRequestId(request.getRequestId());
        comOpeningAcct.setSource(request.getPartnerSdk());

        comOpeningAcctRepo.saveAndFlush(comOpeningAcct);
        return comOpeningAcct;

    }

}
