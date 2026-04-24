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
 * Output context:
 *   "emCustomerInfo" → EmCustomerInfo (Nhóm 1)
 *   "emSalaryInfo"   → EmSalaryInfo   (Nhóm 2)
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
        CustInfo custInfo = request.getCust();

        try {
            // Lấy msisdn + idNumber từ MS Customer (T24)
            CustomerInfoT24 custT24 = (CustomerInfoT24) processContext.get("customerInfoMS");

            String idNumber = null;
            String msisdn = null;

            if (custT24 != null) {
                // idNumber từ person.personalID[0].iDCode
                if (custT24.getPerson() != null
                        && custT24.getPerson().getPersonalID() != null
                        && !custT24.getPerson().getPersonalID().isEmpty()) {
                    idNumber = custT24.getPerson().getPersonalID().get(0).getIDCode();
                }
                // msisdn từ contactInfo.phone[0].phoneNo
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

            log.info("[SA INIT - GET CUST FROM EM] Start - requestId:{}, cifId:{}, msisdn:{}",
                    request.getRequestId(), custInfo.getHostCifId(), msisdn);

            // Call eMoney API customer/info
            ExcuteEmoney<EmCustInfoData> emResponse = apiEmoney.getCustomerInfo(
                    msisdn, idNumber, request.getRequestId());

            // Handle null response (timeout)
            if (Objects.isNull(emResponse)) {
                log.error("[SA INIT - GET CUST FROM EM] Response null (timeout) - requestId:{}",
                        request.getRequestId());
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getCode(), false,
                        ResponseCode.REQUEST_TIMEOUT.getDesc());
                processContext.setResult(result);
                return true;
            }

            // Handle error response (status != 0)
            if (emResponse.getStatus() == null || emResponse.getStatus() != 0) {
                log.error("[SA INIT - GET CUST FROM EM] Error - requestId:{}, status:{}, code:{}, message:{}",
                        request.getRequestId(), emResponse.getStatus(),
                        emResponse.getCode(), emResponse.getMessage());

                // Lookup trực tiếp — enum code = eMoney code
                ResponseCode errorCode = ResponseCode.valueOfErrorCode(emResponse.getCode());
                if (errorCode == null) {
                    errorCode = ResponseCode.TRANSACTION_FAIL;
                }
                result = new SimpleResult(errorCode.getCode(), false, errorCode.getDesc());
                processContext.setResult(result);
//                return true;
            }

            // Success — parse data
            EmCustInfoData data = emResponse.getData();
            if (Objects.isNull(data) || Objects.isNull(data.getCustomerInfo())) {
                log.error("[SA INIT - GET CUST FROM EM] Data/customerInfo is null - requestId:{}",
                        request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        ResponseCode.TRANSACTION_FAIL.getDesc());
                processContext.setResult(result);
//                return true;
            }

            // Put vào context
            processContext.put("emCustomerInfo", data.getCustomerInfo());
            processContext.put("emSalaryInfo", data.getSalaryInfo());

            log.info("[SA INIT - GET CUST FROM EM] Success - requestId:{}, customerId:{}",
                    request.getRequestId(), data.getCustomerInfo().getCustomerId());

        } catch (Exception e) {
            log.error("[SA INIT - GET CUST FROM EM] Exception - requestId:{}, desc:{}",
                    request.getRequestId(), JSON.stringify(e));
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    ResponseCode.TRANSACTION_FAIL.getDesc());
        }

        processContext.setResult(result);
        return !result.isOk();
    }

}
