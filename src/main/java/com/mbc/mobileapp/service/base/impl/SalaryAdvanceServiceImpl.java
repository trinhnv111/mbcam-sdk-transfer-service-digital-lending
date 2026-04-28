package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.api.models.customer.T24CustomerInfo;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.salary_advance.output.CustInfoOutput;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustomerInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetSaLimitResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceInitResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceVerifyOtpResponse;
import com.mbc.mobileapp.service.base.SalaryAdvanceService;
import com.mbc.mobileapp.service.salary_advance.GetSaLimitService;
import com.mbc.mobileapp.service.salary_advance.SalaryAdvanceCreateService;
import com.mbc.mobileapp.service.salary_advance.SalaryAdvanceInitService;
import com.mbc.mobileapp.service.salary_advance.SalaryAdvanceVerifyOtpService;
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
    private final SalaryAdvanceVerifyOtpService salaryAdvanceVerifyOtpService;


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
                // Lấy data từ context
                EmCustomerInfo emCustInfo = (EmCustomerInfo) context.get("emCustomerInfo");
                String tempRecordId = (String) context.get("tempRecordId");
                CustomerInfoT24 custT24 = (CustomerInfoT24) context.get("customerInfoMS");

                // Build response trả FE
                CustInfoOutput custInfoOutput = buildCustInfoOutput(emCustInfo, cust, tempRecordId, custT24);
                response.setCustInfo(custInfoOutput);
            }
        } catch (Exception e) {
            log.error(e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;
    }

    @Override
    public SalaryAdvanceCreateResponse create(CommonServiceRequest request, CustInfo custInfo) {
        SalaryAdvanceCreateResponse response = new SalaryAdvanceCreateResponse();
        try {
            ProcessContext context = loadContext(request, custInfo);

            salaryAdvanceCreateService.execute(context);
            if (context.getResult() != null && !context.getResult().isOk()) {
                response.setResult(context.getResult());
            } else {
                response.setResult(Validator.Result.OK);
            }

        } catch (Exception e) {
            log.error("[CREATE SALARY ADVANCE] fail: ", e);
            response.setResult(new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode()));
        }
        return response;
    }

    @Override
    public SalaryAdvanceVerifyOtpResponse verifyOtp(CommonServiceRequest request, CustInfo custInfo) {
        SalaryAdvanceVerifyOtpResponse response = new SalaryAdvanceVerifyOtpResponse();
        try {
            ProcessContext context = loadContext(request, custInfo);

            salaryAdvanceVerifyOtpService.execute(context);
            if (context.getResult() != null && !context.getResult().isOk()) {
                response.setResult(context.getResult());
            } else {
                Double limit = (Double) context.get("sa_limit");
                String currency = (String) context.get("sa_currency");
                response.setLimit(limit);
                response.setCurrency(currency);
                response.setResult(Validator.Result.OK);
            }

        } catch (Exception e) {
            log.error("[VERIFY OTP SALARY ADVANCE] fail: ", e);
            response.setResult(new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode()));
        }
        return response;
    }

    /**
     * Build CustInfoOutput từ data eMoney + MS Customer+ session
     *   - fullName      : eMoney familyName+firstName
     *   - idNumber       : eMoney customerInfo.idNumber
     *   - phoneNumber    : msCustomer
     *   - email          : input
     *   - maritalStatus  :  input
     *   - placeOfBirth   :  input
     *   - currentAddress :  msCustomer
     */
    private CustInfoOutput buildCustInfoOutput(EmCustomerInfo emCustInfo, CustInfo custInfo,
                                                String tempRecordId, CustomerInfoT24 custT24) {
        CustInfoOutput output = new CustInfoOutput();
        output.setTempRecordId(tempRecordId);

        // Fullname: eMoney familyName + firstName
        String emFullName = emCustInfo.getFamilyName() + " " + emCustInfo.getFirstName();
        output.setFullName(emFullName);
//        if (custT24 != null && custT24.getCustomerName() != null
//                && !Utility.isNull(custT24.getCustomerName().getEngName())) {
//            output.setFullName(custT24.getCustomerName().getEngName());
//        }

        // idNumber
        output.setIdNumber(emCustInfo.getIdNumber());

        // phoneNumber (msCustomer)
        output.setPhoneNumber(custInfo.getPhoneNo());

        // email
//        if (custT24 != null && custT24.getContactInfo() != null
//                && !Utility.isNull(custT24.getContactInfo().getEmailAddress())) {
//            output.setEmail(custT24.getContactInfo().getEmailAddress());
//        }

        // maritalStatus
//        if (custT24 != null && !Utility.isNull(custT24.getMaritalStatus())) {
//            output.setMaritalStatus(custT24.getMaritalStatus());
//        }
//
//        // Place of Birth
//        if (!Utility.isNull(custInfo.getIdTypPlace())) {
//            output.setPlaceOfBirth(custInfo.getIdTypPlace());
//        }

        // Current Address: T24 customerAddress
        CustInfoOutput.Address currentAddress = new CustInfoOutput.Address();
        if (custT24 != null && custT24.getCustomerAddress() != null) {
            currentAddress.setProvince(custT24.getCustomerAddress().getProvinceCode());
            currentAddress.setDistrict(custT24.getCustomerAddress().getDistrictCode());
            currentAddress.setCommune(custT24.getCustomerAddress().getWardCode());
        }
        output.setCurrentAddress(currentAddress);

        return output;
    }
}
