package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.JSON;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.ApiEMoney;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoData;
import com.mbc.mobileapp.api.model.salary_advance.output.ExcuteEmoney;
import com.mbc.mobileapp.constant.SalaryAdvanceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Command: Gọi eMoney API customer/info
 * POST /{merchantCode}/digital-lending/customer/info
 *
 * Input: RSA encrypt (msisdn|idNumber)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoGetCustInfoFromEM implements Command {

    private final ApiEMoney apiEmoney;


    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = processContext.getCustomer();

        try {
            // Lấy msisdn + idNumber từ MS Customer
            CustomerInfoT24 custT24 = (CustomerInfoT24) processContext.get("customerInfoMS");

            String idNumber = null;
            String msisdn = null;

            if (custT24 != null) {
                // idNumber
                if (custT24.getPerson() != null
                        && custT24.getPerson().getPersonalID() != null
                        && !custT24.getPerson().getPersonalID().isEmpty()) {
                    idNumber = custT24.getPerson().getPersonalID().get(0).getIDCode();
                }
                // msisdn
                if (custT24.getContactInfo() != null
                        && custT24.getContactInfo().getPhone() != null
                        && !custT24.getContactInfo().getPhone().isEmpty()) {
                    msisdn = custT24.getContactInfo().getPhone().get(0).getPhoneNo();
                }
            }

            // Fallback
            if (Utility.isNull(idNumber)) {
                idNumber = custInfo.getIdTypNo();
            }
            if (Utility.isNull(msisdn)) {
                msisdn = custInfo.getPhoneNo();
            }

            msisdn = toMsisdn855(msisdn);

            log.info("[SA INIT - GET CUST FROM EM] Start - requestId:{}, cifId:{}, msisdn:{}",
                    request.getRequestId(), custInfo.getHostCifId(), msisdn);

            // Call eMoney API customer/info
            ExcuteEmoney<EmCustInfoData> emResponse = apiEmoney.getCustomerInfo(
                    msisdn, idNumber, custInfo.getId(), request.getRequestId());

            // timeout
            if (Objects.isNull(emResponse)) {
                log.error("[SA INIT - GET CUST FROM EM] Response null (timeout) - requestId:{}",
                        request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                processContext.setResult(result);
                return true;
            }

            // Handle error response (status != 0)
            if (emResponse.getStatus() == null || emResponse.getStatus() != 0) {
                log.error("[SA INIT - GET CUST FROM EM] Error - requestId:{}, status:{}, code:{}, message:{}",
                        request.getRequestId(), emResponse.getStatus(),
                        emResponse.getCode(), emResponse.getMessage());


                ResponseCode errorCode;

                String emCode = emResponse.getCode();
                String emMsg = emResponse.getMessage() != null ? emResponse.getMessage() : "";

                // Map specific eMoney errors to MBC Response Codes
                if ("ERR_NOT_RECEIVE_SALARY_6M".equals(emCode)
                        || emMsg.contains("Customer does not receive salary")) {
                    errorCode = ResponseCode.SA_CREDIT_REJECTED;
                } else if ("ERR_CUSTOMER_WRONG_INFOR".equals(emCode)) {
                    errorCode = ResponseCode.SA_ID_MISMATCH;
                }
                else if ("ERR_CUSTOMER_INACTIVE".equals(emCode)){
                    errorCode = ResponseCode.SA_CREDIT_REJECTED;
                }
                else if ("ERR_CUSTOMER_NOT_FOUND".equals(emCode)) {
                    errorCode = ResponseCode.SA_CREDIT_REJECTED;
                }

                else {
                    errorCode = ResponseCode.valueOfErrorCode(emCode);
                    if (errorCode == null) {
                        errorCode = ResponseCode.SA_GENERAL_ERROR;
                    }
                }

                result = new SimpleResult(errorCode.getDesc(), false, errorCode.getCode());
                processContext.setResult(result);
                return true;
            }

            // Success — parse data
            EmCustInfoData data = emResponse.getData();
            if (Objects.isNull(data) || Objects.isNull(data.getCustomerInfo())) {
                log.error("[SA INIT - GET CUST FROM EM] Data/customerInfo is null - requestId:{}",
                        request.getRequestId());
                result = new SimpleResult(ResponseCode.SA_GENERAL_ERROR.getDesc(), false,
                        ResponseCode.SA_GENERAL_ERROR.getCode());
                processContext.setResult(result);
                return true;
            }

            // Put vào context
            processContext.put("emCustomerInfo", data.getCustomerInfo());
            processContext.put("emSalaryInfo", data.getSalaryInfo());

            log.info("[SA INIT - GET CUST FROM EM] Success - requestId:{}, customerId:{}",
                    request.getRequestId(), data.getCustomerInfo().getCustomerId());

        } catch (Exception e) {
            log.error("[SA INIT - GET CUST FROM EM] Exception - requestId:{}, desc:{}",
                    request.getRequestId(), JSON.stringify(e));
            result = new SimpleResult(ResponseCode.SA_GENERAL_ERROR.getDesc(), false,
                    ResponseCode.SA_GENERAL_ERROR.getCode());
        }

        processContext.setResult(result);
        return !result.isOk();
    }

    public static String toMsisdn855(String msisdn) {
        if (msisdn == null) {
            return null;
        }

        msisdn = msisdn.trim();

        if (msisdn.isEmpty()) {
            return null;
        }

        if (msisdn.startsWith(SalaryAdvanceConstant.MSISDN855)) {
            return msisdn;
        }

        if (msisdn.startsWith("0")) {
            return SalaryAdvanceConstant.MSISDN855 + msisdn.substring(1);
        }

        return SalaryAdvanceConstant.MSISDN855 + msisdn;
    }

}
