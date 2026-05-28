package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.common.services.il.customerinfo.CustomerAddress;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.salary_advance.output.CustInfoOutput;
import com.mbc.mobileapp.constant.MaritalStatus;
import com.mbc.mobileapp.constant.SalaryAdvanceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.*;
import com.mbc.mobileapp.service.base.SalaryAdvanceService;
import com.mbc.common.repository.ComProvinceRepo;
import com.mbc.common.repository.ComDistrictRepo;
import com.mbc.common.repository.ComWardRepo;
import com.mbc.common.repository.ComCountryRepo;
import com.mbc.common.entity.ComCountry;
import com.mbc.common.dto.Province;
import com.mbc.common.dto.District;
import com.mbc.common.dto.Ward;
import com.mbc.mobileapp.service.salary_advance.GetSalaryAdvanceOfferLimitService;
import com.mbc.mobileapp.service.salary_advance.SalaryAdvanceCreateService;
import com.mbc.mobileapp.service.salary_advance.SalaryAdvanceInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryAdvanceServiceImpl extends ServiceBase implements SalaryAdvanceService {
    private final SalaryAdvanceInitService salaryAdvanceInitService;
    private final GetSalaryAdvanceOfferLimitService getSalaryAdvanceOfferLimitService;
    private final SalaryAdvanceCreateService salaryAdvanceCreateService;
    private final ComProvinceRepo comProvinceRepo;
    private final ComDistrictRepo comDistrictRepo;
    private final ComWardRepo comWardRepo;
    private final ComCountryRepo comCountryRepo;


    @Override
    public GetSalaryAdvanceOfferLimitResponse getSalaryAdvanceOfferLimit(CommonServiceRequest request, CustInfo custInfo) {
        GetSalaryAdvanceOfferLimitResponse resp = new GetSalaryAdvanceOfferLimitResponse();
        ProcessContext processContext = loadContext(request, custInfo);
        Validator.Result result;
        try {
            getSalaryAdvanceOfferLimitService.execute(processContext);
            logService.execute(processContext);
            result = processContext.getResult();
            resp.setResult(result);
            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) processContext.getResponse();
                resp.setData(res.getSalaryAdvanceOfferLimitData());
            }

        } catch (Exception e) {
            log.error(e.toString());
            processContext.setResult(Validator.Result.UNKNOWN);
        }

        return resp;
    }

    @Override
    public SalaryAdvanceInitResponse init(CommonServiceRequest request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        SalaryAdvanceInitResponse response = new SalaryAdvanceInitResponse();
        Validator.Result result;

        try {
            salaryAdvanceInitService.execute(context);
            logService.execute(context);

            result = context.getResult();
            response.setResult(result);

            if (result.isOk()) {
                CustomerInfoT24 custT24 = (CustomerInfoT24) context.get("customerInfoMS");
                String transId = (String) context.get("transId");

                // null  = chưa từng tạo → FE hiển thị màn hình chọn khuyết tật
                // true/false = đã từng tạo → giá trị KH đã chọn lần trước
                Boolean showDisability = (Boolean) context.get("showDisabilities");

                CustInfoOutput custInfoOutput = buildCustInfoOutput(custT24, cust, request.getLanguage());

                SalaryAdvanceInitData initData = new SalaryAdvanceInitData();
                initData.setTransId(transId);
                initData.setCustInfo(custInfoOutput);
                initData.setShowDisabilities(showDisability);

                response.setData(initData);
            }

        } catch (Exception e) {
            log.error("init salary advance error", e);
            context.setResult(Validator.Result.UNKNOWN);
        }

        return response;
    }

    @Override
    public SalaryAdvanceCreateResponse create(CommonServiceRequest request, CustInfo custInfo, TokenOtp tokenOtp) {
        SalaryAdvanceCreateResponse response = new SalaryAdvanceCreateResponse();
        ProcessContext context = loadContext(request, custInfo);
        try {
            context.putVar(Constant.KeyVar.OTP, tokenOtp);

            salaryAdvanceCreateService.execute(context);
            if (context.getResult() != null && !context.getResult().isOk()) {
                response.setResult(context.getResult());
            } else {
                response.setResult(Validator.Result.OK);
                String transId = request.getSalaryAdvanceCreateRequest().getTransId();
                Double limitAmount = null;
                Object saLimit = context.get("sa_limit");
                if (saLimit instanceof java.math.BigDecimal) {
                    limitAmount = ((java.math.BigDecimal) saLimit).doubleValue();
                } else if (saLimit instanceof Double) {
                    limitAmount = (Double) saLimit;
                }
                String currency = (String) context.get("sa_currency");

                SalaryAdvanceCreateData data = SalaryAdvanceCreateData.builder()
                        .transId(transId)
                        .limitAmount(limitAmount)
                        .currency(currency)
                        .build();
                response.setData(data);
            }

        } catch (Exception e) {
            log.error("[CREATE SALARY ADVANCE] fail: ", e);
            context.setResult(Validator.Result.UNKNOWN);
            response.setResult(new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode()));
        } finally {
            try {
                logService.execute(context);
            } catch (Exception ex) {
                log.error("[CREATE SALARY ADVANCE] logService error: ", ex);
            }
        }
        return response;
    }


    /**
     * Build CustInfoOutput từ MS Customer ({@code DoGetCustInfroFromMSCust}).
     */
    private CustInfoOutput buildCustInfoOutput(CustomerInfoT24 custT24, CustInfo custInfo, String language) {
        CustInfoOutput output = new CustInfoOutput();

        if (custT24 == null) {
            output.setIdNumber(custInfo.getIdTypNo());
            output.setPhoneNumber(custInfo.getPhoneNo());
            return output;
        }

        if (custT24.getPerson() != null
                && custT24.getPerson().getPersonalID() != null
                && !custT24.getPerson().getPersonalID().isEmpty()) {
            output.setIdNumber(custT24.getPerson().getPersonalID().get(0).getIDCode());
        }
        if (Utility.isNull(output.getIdNumber())) {
            output.setIdNumber(custInfo.getIdTypNo());
        }

        if (custT24.getCustomerName() != null) {
            output.setEnglishName(custT24.getCustomerName().getVnName());
        }

        if (custT24.getContactInfo() != null
                && custT24.getContactInfo().getPhone() != null
                && !custT24.getContactInfo().getPhone().isEmpty()) {
            output.setPhoneNumber(custT24.getContactInfo().getPhone().get(0).getPhoneNo());
        }
        if (Utility.isNull(output.getPhoneNumber())) {
            output.setPhoneNumber(custInfo.getPhoneNo());
        }

        if (custT24.getContactInfo() != null) {
            output.setEmail(custT24.getContactInfo().getEmailAddress());
        }

//        if (custT24.getPerson().getResidence() != null) {
//            output.setPlaceOfBirthCountry(custT24.getResidence());
//        }


        if (custT24.getPerson() != null && custT24.getPerson().getResidence() != null) {
            output.setPlaceOfBirthCountry(custT24.getPerson().getResidence());
        }


        // Map maritalStatusCode — chuyển text T24 (VD: "Single") thành mã code (VD: "S") để client tự xử lý đa ngôn ngữ
        String rawMarital = null;
        if (!Utility.isNull(custT24.getMaritalStatus())) {
            rawMarital = custT24.getMaritalStatus();
        } else if (custT24.getPerson() != null && !Utility.isNull(custT24.getPerson().getMaritalStatus())) {
            rawMarital = custT24.getPerson().getMaritalStatus();
        }
        if (rawMarital != null) {
            output.setMaritalStatus(MaritalStatus.toCode(rawMarital));
        }

        CustomerAddress currentAddress = findAddressByType(custT24, "Current");
        if (currentAddress != null) {
            output.setCurrentCountry(getCountryNameFromRepo(currentAddress.getCountryCode(), currentAddress.getCountry(), language));
            if (SalaryAdvanceConstant.COUNTRY_NAME_CAMBODIA.equalsIgnoreCase(currentAddress.getCountry())) {
                output.setCurrentProvince(Utility.isNull(currentAddress.getProvinceCode()) ? null : currentAddress.getProvinceCode());
                output.setCurrentDistrict(Utility.isNull(currentAddress.getDistrictCode()) ? null : currentAddress.getDistrictCode());
                output.setCurrentCommune(Utility.isNull(currentAddress.getWardCode()) ? null : currentAddress.getWardCode());
                output.setCurrentProvinceName(output.getCurrentProvince() != null ? "" : null);
                output.setCurrentDistrictName(output.getCurrentDistrict() != null ? "" : null);
                output.setCurrentCommuneName(output.getCurrentCommune() != null ? "" : null);

                if (!Utility.isNull(output.getCurrentProvince())) {
                    Province p = comProvinceRepo.getByProvinceCode(output.getCurrentProvince());
                    if (p != null) output.setCurrentProvinceName(isKhmerLanguage(language) ? p.getProvinceNameKh() : p.getProvinceName());
                }
                if (!Utility.isNull(output.getCurrentDistrict())) {
                    District d = comDistrictRepo.findByDistrictCode(output.getCurrentDistrict());
                    if (d != null) output.setCurrentDistrictName(isKhmerLanguage(language) ? d.getDistrictNameKh() : d.getDistrictName());
                }
                if (!Utility.isNull(output.getCurrentCommune())) {
                    Ward w = comWardRepo.findByWardCode(output.getCurrentCommune());
                    if (w != null) output.setCurrentCommuneName(isKhmerLanguage(language) ? w.getWardNameKh() : w.getWardName());
                }
            }
        }

        CustomerAddress residenceAddress = findAddressByType(custT24, "Residence");
        if (residenceAddress != null) {
            String residenceCountryName = residenceAddress.getCountry();

            // Lấy country code đã gán từ getPerson().getResidence() ở trên, nếu null thì thử lấy từ custT24.getResidence()
            String residenceCountryCode = output.getPlaceOfBirthCountry();
            if (Utility.isNull(residenceCountryCode)) {
                residenceCountryCode = custT24.getResidence();
                output.setPlaceOfBirthCountry(residenceCountryCode);
            }

            output.setPlaceOfBirthCountryName(getCountryNameFromRepo(residenceCountryCode, residenceCountryName, language));

            // Check if Cambodia: if residence is explicitly KH, or (residence is null/empty AND country is CAMBODIA)
            boolean isCambodia = SalaryAdvanceConstant.LANG_KH.equalsIgnoreCase(residenceCountryCode)
                    || (Utility.isNull(residenceCountryCode) && SalaryAdvanceConstant.COUNTRY_NAME_CAMBODIA.equalsIgnoreCase(residenceCountryName))
                    || SalaryAdvanceConstant.COUNTRY_NAME_CAMBODIA.equalsIgnoreCase(residenceCountryName) && SalaryAdvanceConstant.LANG_KH.equalsIgnoreCase(residenceAddress.getCountryCode());

            if (isCambodia) {
                output.setPlaceOfBirthProvince(Utility.isNull(residenceAddress.getProvinceCode()) ? null : residenceAddress.getProvinceCode());
                output.setPlaceOfBirthDistrict(Utility.isNull(residenceAddress.getDistrictCode()) ? null : residenceAddress.getDistrictCode());
                output.setPlaceOfBirthCommune(Utility.isNull(residenceAddress.getWardCode()) ? null : residenceAddress.getWardCode());
                output.setPlaceOfBirthProvinceName(output.getPlaceOfBirthProvince() != null ? "" : null);
                output.setPlaceOfBirthDistrictName(output.getPlaceOfBirthDistrict() != null ? "" : null);
                output.setPlaceOfBirthCommuneName(output.getPlaceOfBirthCommune() != null ? "" : null);

                if (!Utility.isNull(output.getPlaceOfBirthProvince())) {
                    Province p = comProvinceRepo.getByProvinceCode(output.getPlaceOfBirthProvince());
                    if (p != null) output.setPlaceOfBirthProvinceName(isKhmerLanguage(language) ? p.getProvinceNameKh() : p.getProvinceName());
                }
                if (!Utility.isNull(output.getPlaceOfBirthDistrict())) {
                    District d = comDistrictRepo.findByDistrictCode(output.getPlaceOfBirthDistrict());
                    if (d != null) output.setPlaceOfBirthDistrictName(isKhmerLanguage(language) ? d.getDistrictNameKh() : d.getDistrictName());
                }
                if (!Utility.isNull(output.getPlaceOfBirthCommune())) {
                    Ward w = comWardRepo.findByWardCode(output.getPlaceOfBirthCommune());
                    if (w != null) output.setPlaceOfBirthCommuneName(isKhmerLanguage(language) ? w.getWardNameKh() : w.getWardName());
                }
            } else {
                output.setPlaceOfBirthProvince(null);
                output.setPlaceOfBirthProvinceName(null);
                output.setPlaceOfBirthDistrict(null);
                output.setPlaceOfBirthDistrictName(null);
                output.setPlaceOfBirthCommune(null);
                output.setPlaceOfBirthCommuneName(null);
            }
        }


        return output;
    }



    private String getCountryNameFromRepo(String countryCode, String fallbackName, String language) {
        if (!Utility.isNull(countryCode)) {
            ComCountry country = comCountryRepo.findById(countryCode).orElse(null);
            if (country != null) {
                return isKhmerLanguage(language) ? country.getNameKh() : country.getName();
            }
        }
        return formatCountryName(fallbackName);
    }

    private static CustomerAddress findAddressByType(CustomerInfoT24 custT24, String addressTypeCode) {
        if (custT24.getContactInfo() == null || custT24.getContactInfo().getAddress() == null) {
            return null;
        }
        return custT24.getContactInfo().getAddress().stream()
                .filter(address -> addressTypeCode.equals(address.getAddressTypeCode()))
                .findFirst()
                .orElse(null);
    }


    private static String formatCountryName(String country) {
        if (Utility.isNull(country)) {
            return country;
        }
        country = country.trim();
        if (country.isEmpty()) {
            return country;
        }
        return country.substring(0, 1).toUpperCase() + country.substring(1).toLowerCase();
    }

    private boolean isKhmerLanguage(String language) {
        if (Utility.isNull(language)) {
            return false;
        }
        String lang = language.trim().toUpperCase();
        return SalaryAdvanceConstant.LANG_KH.equals(lang)
                || SalaryAdvanceConstant.LANG_KHM.equals(lang)
                || SalaryAdvanceConstant.LANG_KM.equals(lang);
    }
}
