package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.JSON;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustomerInfo;
import com.mbc.mobileapp.api.model.salary_advance.output.EmSalaryInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Command: Validate thông tin khách hàng từ eMoney
 * 1. Match National ID (session vs eMoney)
 * 2. Tuổi >= 18
 * 3. Lương liên tục >= 6 tháng (từ salaryInfo.continuousSalary6Months)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoValidateSalaryCust implements Command {

    private static final int MIN_AGE = 18;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = processContext.getCustomer();

        try {
            log.info("[SA INIT - VALIDATE] Start - requestId:{}", request.getRequestId());

            EmCustomerInfo emCustInfo = (EmCustomerInfo) processContext.get("emCustomerInfo");
            EmSalaryInfo emSalaryInfo = (EmSalaryInfo) processContext.get("emSalaryInfo");

            if (Objects.isNull(emCustInfo)) {
                log.error("[SA INIT - VALIDATE] emCustomerInfo is null");
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        ResponseCode.TRANSACTION_FAIL.getDesc());
                processContext.setResult(result);
                return true;
            }

            // 1. Validate nationalId: session (idTypNo) vs eMoney (idNumber)
            String sessionIdNumber = custInfo.getIdTypNo();
            String emIdNumber = emCustInfo.getIdNumber();

            if (Utility.isNull(sessionIdNumber) || !sessionIdNumber.equals(emIdNumber)) {
                log.error("[SA INIT - VALIDATE] ID mismatch: session={}, eMoney={}", sessionIdNumber, emIdNumber);
                result = new SimpleResult("idNumber mismatch", false, ResponseCode.IDTYPNO_NOT_VALID.getDesc());
                processContext.setResult(result);
                return true;
            }

            // 2. Check tuổi >= 18
            String dobStr = emCustInfo.getDateOfBirth();
            if (Utility.isNull(dobStr)) {
                log.error("[SA INIT - VALIDATE] DOB is null");
                result = new SimpleResult("DOB is null", false, ResponseCode.TRANSACTION_FAIL.getDesc());
                processContext.setResult(result);

            }

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date dob = sdf.parse(dobStr);
            Calendar dobCal = Calendar.getInstance();
            dobCal.setTime(dob);
            Calendar now = Calendar.getInstance();
            int age = now.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (now.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            if (age < MIN_AGE) {
                log.error("[SA INIT - VALIDATE] Age invalid: {}", age);
                result = new SimpleResult("AGE_NOT_VALID", false, ResponseCode.IDTYPNO_NOT_VALID.getDesc());
                processContext.setResult(result);
                return true;

            }

            // 3. Check lương liên tục 6 tháng (từ salaryInfo)
            if (Objects.isNull(emSalaryInfo)
                    || Boolean.FALSE.equals(emSalaryInfo.getContinuousSalary6Months())
                    || Objects.isNull(emSalaryInfo.getSalary3mAvgUSD())
                    || emSalaryInfo.getSalary3mAvgUSD().signum() <= 0) {
                log.error("[SA INIT - VALIDATE] Salary invalid: continuousSalary6Months={}, salary3mAvgUSD={}",
                        emSalaryInfo != null ? emSalaryInfo.getContinuousSalary6Months() : null,
                        emSalaryInfo != null ? emSalaryInfo.getSalary3mAvgUSD() : null);
                result = new SimpleResult("SALARY_INVALID", false, ResponseCode.TRANSACTION_FAIL.getDesc());
                processContext.setResult(result);
                return true;
            }

            log.info("[SA INIT - VALIDATE] Passed - requestId:{}", request.getRequestId());
//            return true;

        } catch (Exception e) {
            log.error("[SA INIT - VALIDATE] Exception - requestId:{}, desc:{}",
                    request.getRequestId(), JSON.stringify(e));
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    ResponseCode.TRANSACTION_FAIL.getDesc());
        }

        processContext.setResult(result);
        return !result.isOk();
    }
}
