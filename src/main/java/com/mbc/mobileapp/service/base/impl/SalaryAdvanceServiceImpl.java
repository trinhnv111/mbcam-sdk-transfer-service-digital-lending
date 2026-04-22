package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.api.model.salary_advance.output.CustInfoOutput;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetSaLimitResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceInitResponse;
import com.mbc.mobileapp.service.base.SalaryAdvanceService;
import com.mbc.mobileapp.service.salary_advance.GetSaLimitService;
import com.mbc.mobileapp.service.salary_advance.SalaryAdvanceInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Service
@RequiredArgsConstructor

public class SalaryAdvanceServiceImpl extends ServiceBase implements SalaryAdvanceService {
    private final SalaryAdvanceInitService salaryAdvanceInitService;
    private final GetSaLimitService getSaLimitService;



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
            if(result.isOk()){
                CommonServiceResponse res =(CommonServiceResponse) processContext.getResponse();
                resp.setData(res.getSaLimitData());

            }


        } catch (Exception e){
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
                // Lấy data từ context (đã được các command set vào)
                EmCustInfoOutput emCustInfo = (EmCustInfoOutput) context.get("emCustInfoOutput");
                String tempRecordId = (String) context.get("tempRecordId");
                CustomerInfoT24 customerInfoT24 = (CustomerInfoT24) context.get("customerInfoMS");

                // Build response trả FE
                CustInfoOutput custInfoOutput = buildCustInfoOutput(emCustInfo, cust, tempRecordId, customerInfoT24);
                response.setCustInfo(custInfoOutput);
            }
        } catch (Exception e) {
            log.error(e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;
    }

    /**
     * Build CustInfoOutput từ data eMoney + MS Customer (T24) + session
     * Ưu tiên: T24 > eMoney > session (nếu có)
     */
    private CustInfoOutput buildCustInfoOutput(EmCustInfoOutput emCustInfo, CustInfo custInfo,
                                                String tempRecordId, CustomerInfoT24 custT24) {
        CustInfoOutput output = new CustInfoOutput();
        output.setTempRecordId(tempRecordId);

        // Fullname: ưu tiên T24 engName, fallback eMoney englishName
        output.setFullName(emCustInfo.getEnglishName());
        if (custT24 != null && custT24.getCustomerName() != null
                && !Utility.isNull(custT24.getCustomerName().getEngName())) {
            output.setFullName(custT24.getCustomerName().getEngName());
        }

        output.setIdNumber(emCustInfo.getIdNumber());
        output.setPhoneNumber(emCustInfo.getPhoneNumber());
        output.setEmail(emCustInfo.getEmail());
        output.setEmployStartDate(emCustInfo.getEmploymentDate());
        output.setMaritalStatus(emCustInfo.getMaritalStatus());

        // Place of Birth (country): session ưu tiên > eMoney
        output.setPlaceOfBirth(emCustInfo.getPlaceOfBirthCountry());
        if (!Utility.isNull(custInfo.getIdTypPlace())) {
            output.setPlaceOfBirth(custInfo.getIdTypPlace());
        }

        // Place of Birth Details (Province > District > Commune) — từ eMoney
        CustInfoOutput.Address placeOfBirthAddr = new CustInfoOutput.Address();
        placeOfBirthAddr.setProvince(emCustInfo.getPlaceOfBirthProvince());
        placeOfBirthAddr.setDistrict(emCustInfo.getPlaceOfBirthDistrict());
        placeOfBirthAddr.setCommune(emCustInfo.getPlaceOfBirthCommune());
        output.setPlaceOfBirthAddress(placeOfBirthAddr);

        // Current Address: ưu tiên T24 (customerAddress), fallback eMoney (residential*)
        CustInfoOutput.Address currentAddress = new CustInfoOutput.Address();
        if (custT24 != null && custT24.getCustomerAddress() != null) {
            currentAddress.setProvince(custT24.getCustomerAddress().getProvinceCode());
            currentAddress.setDistrict(custT24.getCustomerAddress().getDistrictCode());
            currentAddress.setCommune(custT24.getCustomerAddress().getWardCode());
        } else {
            currentAddress.setProvince(emCustInfo.getResidentialProvince());
            currentAddress.setDistrict(emCustInfo.getResidentialDistrict());
            currentAddress.setCommune(emCustInfo.getResidentialCommune());
        }
        output.setCurrentAddress(currentAddress);

        return output;
    }
}
