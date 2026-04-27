package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.api.model.salary_advance.output.CustInfoOutput;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustomerInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.GetSaLimitResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceInitResponse;
import com.mbc.mobileapp.service.base.SalaryAdvanceService;
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
    public SalaryAdvanceCreateResponse create(CommonServiceRequest request, CustInfo cust, TokenOtp tokenOtp) {
        ProcessContext context = loadContext(request, cust);

        // Đặt TokenOtp vào context — ValidateOTP sẽ đọc từ đây
        context.putVar(Constant.KeyVar.OTP, tokenOtp);

        SalaryAdvanceCreateResponse response = new SalaryAdvanceCreateResponse();
        Validator.Result result;
        try {
            salaryAdvanceCreateService.execute(context);
            logService.execute(context);
            result = context.getResult();
            response.setResult(result);

            if (result.isOk()) {
                // Lấy response data từ context (DoSaveSalaryAdvanceRecord đã build)
                SalaryAdvanceCreateResponse createData =
                        (SalaryAdvanceCreateResponse) context.get("createResponse");

                if (createData != null) {
                    response.setCustId(createData.getCustId());
                    response.setHostCifId(createData.getHostCifId());
                    response.setCustomerName(createData.getCustomerName());
                    response.setNationalId(createData.getNationalId());
                    response.setIdType(createData.getIdType());
                    response.setPhoneNumber(createData.getPhoneNumber());
                    response.setEmail(createData.getEmail());
                    response.setAddressProvince(createData.getAddressProvince());
                    response.setAddressDistrict(createData.getAddressDistrict());
                    response.setAddressWard(createData.getAddressWard());
                    response.setMaritalStatus(createData.getMaritalStatus());
                    response.setHasUsdAccount(createData.getHasUsdAccount());
                    response.setUsdAccountNo(createData.getUsdAccountNo());
                    response.setLimit(createData.getLimit());
                    response.setCurrency(createData.getCurrency());
                }
            }
        } catch (Exception e) {
            log.error("[SA CREATE] Exception: {}", e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;
    }

    /**
     * Build CustInfoOutput từ data eMoney + MS Customer+ session
     */
    private CustInfoOutput buildCustInfoOutput(EmCustomerInfo emCustInfo, CustInfo custInfo,
                                                String tempRecordId, CustomerInfoT24 custT24) {
        CustInfoOutput output = new CustInfoOutput();
        output.setTempRecordId(tempRecordId);

        // Fullname: eMoney familyName + firstName
        String emFullName = emCustInfo.getFamilyName() + " " + emCustInfo.getFirstName();
        output.setFullName(emFullName);

        // idNumber
        output.setIdNumber(emCustInfo.getIdNumber());

        // phoneNumber (msCustomer)
        output.setPhoneNumber(custInfo.getPhoneNo());

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
