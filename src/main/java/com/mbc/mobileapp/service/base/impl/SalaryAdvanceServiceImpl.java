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
import com.mbc.common.services.il.customerinfo.CustomerOccupation;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.salary_advance.output.CustInfoOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.*;
import com.mbc.mobileapp.service.base.SalaryAdvanceService;
import com.mbc.common.repository.ComProvinceRepo;
import com.mbc.common.repository.ComDistrictRepo;
import com.mbc.common.repository.ComWardRepo;
import com.mbc.common.dto.Province;
import com.mbc.common.dto.District;
import com.mbc.common.dto.Ward;
import com.mbc.mobileapp.service.salary_advance.GetSaLimitService;
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
    private final GetSaLimitService getSaLimitService;
    private final SalaryAdvanceCreateService salaryAdvanceCreateService;
    private final ComProvinceRepo comProvinceRepo;
    private final ComDistrictRepo comDistrictRepo;
    private final ComWardRepo comWardRepo;


    @Override
    public GetSaLimitResponse getSaLimit(CommonServiceRequest request, CustInfo custInfo) {
        GetSaLimitResponse resp = new GetSaLimitResponse();
        ProcessContext processContext = loadContext(request, custInfo);
        Validator.Result result;
        try {
            getSaLimitService.execute(processContext);
            logService.execute(processContext);
            result = processContext.getResult();
            resp.setResult(result);
            if (result.isOk()) {
                CommonServiceResponse res = (CommonServiceResponse) processContext.getResponse();
                resp.setData(res.getSaLimitData());
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

//        if (custT24.getMaritalStatus() != null) {
//            output.setMaritalStatus(custT24.getMaritalStatus());
//        } else if (custT24.getPerson() != null && custT24.getPerson().getMaritalStatus() != null) {
//            output.setMaritalStatus(custT24.getPerson().getMaritalStatus());
//        }



        CustomerAddress currentAddress = findAddressByType(custT24, "Current");
        if (currentAddress != null) {
            output.setCurrentCountry(formatCountryName(firstNonBlank(currentAddress.getCountryCode(), currentAddress.getCountry())));
            if ("CAMBODIA".equalsIgnoreCase(currentAddress.getCountry())) {
                output.setCurrentProvince(currentAddress.getProvinceCode());
                output.setCurrentDistrict(currentAddress.getDistrictCode());
                output.setCurrentCommune(currentAddress.getWardCode());
                output.setCurrentProvinceName(output.getCurrentProvince() != null ? "" : null);
                output.setCurrentDistrictName(output.getCurrentDistrict() != null ? "" : null);
                output.setCurrentCommuneName(output.getCurrentCommune() != null ? "" : null);

                if (!Utility.isNull(output.getCurrentProvince())) {
                    Province p = comProvinceRepo.getByProvinceCode(output.getCurrentProvince());
                    if (p != null) output.setCurrentProvinceName("KH".equalsIgnoreCase(language) ? p.getProvinceNameKh() : p.getProvinceName());
                }
                if (!Utility.isNull(output.getCurrentDistrict())) {
                    District d = comDistrictRepo.findByDistrictCode(output.getCurrentDistrict());
                    if (d != null) output.setCurrentDistrictName("KH".equalsIgnoreCase(language) ? d.getDistrictNameKh() : d.getDistrictName());
                }
                if (!Utility.isNull(output.getCurrentCommune())) {
                    Ward w = comWardRepo.findByWardCode(output.getCurrentCommune());
                    if (w != null) output.setCurrentCommuneName("KH".equalsIgnoreCase(language) ? w.getWardNameKh() : w.getWardName());
                }
            }
        }

        CustomerAddress residenceAddress = findAddressByType(custT24, "Residence");
        if (residenceAddress != null) {
            output.setPlaceOfBirthCountryName(formatCountryName(firstNonBlank(residenceAddress.getCountryCode(), residenceAddress.getCountry())));
            if ("CAMBODIA".equalsIgnoreCase(residenceAddress.getCountry())) {
                output.setPlaceOfBirthCountry(custT24.getResidence());
                output.setPlaceOfBirthProvince(residenceAddress.getProvinceCode());
                output.setPlaceOfBirthDistrict(residenceAddress.getDistrictCode());
                output.setPlaceOfBirthCommune(residenceAddress.getWardCode());
                output.setPlaceOfBirthProvinceName(output.getPlaceOfBirthProvince() != null ? "" : null);
                output.setPlaceOfBirthDistrictName(output.getPlaceOfBirthDistrict() != null ? "" : null);
                output.setPlaceOfBirthCommuneName(output.getPlaceOfBirthCommune() != null ? "" : null);

                if (!Utility.isNull(output.getPlaceOfBirthProvince())) {
                    Province p = comProvinceRepo.getByProvinceCode(output.getPlaceOfBirthProvince());
                    if (p != null) output.setPlaceOfBirthProvinceName("KH".equalsIgnoreCase(language) ? p.getProvinceNameKh() : p.getProvinceName());
                }
                if (!Utility.isNull(output.getPlaceOfBirthDistrict())) {
                    District d = comDistrictRepo.findByDistrictCode(output.getPlaceOfBirthDistrict());
                    if (d != null) output.setPlaceOfBirthDistrictName("KH".equalsIgnoreCase(language) ? d.getDistrictNameKh() : d.getDistrictName());
                }
                if (!Utility.isNull(output.getPlaceOfBirthCommune())) {
                    Ward w = comWardRepo.findByWardCode(output.getPlaceOfBirthCommune());
                    if (w != null) output.setPlaceOfBirthCommuneName("KH".equalsIgnoreCase(language) ? w.getWardNameKh() : w.getWardName());
                }
            }
        }


        return output;
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

    private static String firstNonBlank(String primary, String fallback) {
        if (!Utility.isNull(primary)) {
            return primary;
        }
        return fallback;
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
}
