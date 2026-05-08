package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.bean.TokenOtp;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.salary_advance.output.CustInfoOutput;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustomerInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.getloan.*;
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
                EmCustomerInfo emCustInfo = (EmCustomerInfo) context.get("emCustomerInfo");
                String transId = (String) context.get("transId");

                CustInfoOutput custInfoOutput = buildCustInfoOutput(emCustInfo, cust);
                response.setData(SalaryAdvanceInitData.builder()
                        .transId(transId)
                        .custInfo(custInfoOutput)
                        .build());
            }
        } catch (Exception e) {
            log.error(e.toString());
            context.setResult(Validator.Result.UNKNOWN);
        }
        return response;
    }

    @Override
    public SalaryAdvanceCreateResponse create(CommonServiceRequest request, CustInfo custInfo, TokenOtp tokenOtp) {
        SalaryAdvanceCreateResponse response = new SalaryAdvanceCreateResponse();
        try {
            ProcessContext context = loadContext(request, custInfo);
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
            response.setResult(new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode()));
        }
        return response;
    }


    /**
     * Build CustInfoOutput từ data eMoney + MS Customer+ session
     * - fullName      : msCustomer
     * - idNumber       : msCustomer
     * - phoneNumber    : msCustomer
     * - email          : input
     * - maritalStatus  :  input
     * - placeOfBirth   :  input
     * - currentAddress :  input
     */
    private CustInfoOutput buildCustInfoOutput(EmCustomerInfo emCustInfo, CustInfo custInfo) {
        CustInfoOutput output = new CustInfoOutput();

        // fullName
        output.setFullName(custInfo.getNm());

        // idNumber
        output.setIdNumber(custInfo.getIdTypNo());

        // phoneNumber
        output.setPhoneNumber(custInfo.getPhoneNo());

        return output;
    }
}
