package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.JSON;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor

public class DoValidateSalaryCust implements Command {

    private static final int MIN_AGE = 18;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    //2.validate khách hàng
    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest commonServiceRequest = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = commonServiceRequest.getCust();

        try {
            log.info("[SA INIT ] - vailate cust info - requestId :{}", commonServiceRequest.getRequestId());

            EmCustInfoOutput emCustInfoOutput = (EmCustInfoOutput) processContext.get("emCustInfoOutput");

            if (Objects.isNull(emCustInfoOutput)) {
                log.error("[SA INIT ] - vailate cust info null ");
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false, ResponseCode.TRANSACTION_FAIL.getDesc());
                return true;
            }
            // 1 validate nationalId từ idNumber(session)  vs idNumber (em)
            String sessionIdNumber = custInfo.getIdTypNo();
            String emIdNumber = emCustInfoOutput.getIdNumber();

            if (Utility.isNull(sessionIdNumber) || !sessionIdNumber.equals(emIdNumber)) {
                log.error("[SA INIT ] - vailate cust info idNumber mismatch sessionIdNumber:{}, emIdNumber{} ", sessionIdNumber, emIdNumber);
                result = new SimpleResult("idNumber mismatch", false, ResponseCode.IDTYPNO_NOT_VALID.getDesc());

                processContext.setResult(result);
                return true;
            }
            //2 check tuổi
            String dobStr = emCustInfoOutput.getDateOfBirth();
            if (Utility.isNull(dobStr)) {
                log.error("[SA INIT] - DOB is null");
                result = new SimpleResult("DOB is null", false, ResponseCode.TRANSACTION_FAIL.getDesc());
                processContext.setResult(result);
                return true;
            }

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date dob = sdf.parse(dobStr);

            // tính tuổi
            Calendar dobCal = Calendar.getInstance();
            dobCal.setTime(dob);

            Calendar now = Calendar.getInstance();

            int age = now.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);

            // check chưa qua sinh nhật năm nay
            if (now.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            // validate
            if (age < MIN_AGE) {
                log.error("[SA INIT] - age invalid: {}", age);
                // ResponseCode + AGE_NOT_VALID
                result = new SimpleResult("AGE_NOT_VALID", false, ResponseCode.IDTYPNO_NOT_VALID.getDesc());


                processContext.setResult(result);
                return true;
            }

            //3 check lương
            if(Boolean.FALSE.equals(emCustInfoOutput.getSix_months_salary_payments())
                    || emCustInfoOutput.getMonthlySalaryAmountUsd().compareTo(BigDecimal.ZERO) <=0){
                log.error("[SA INIT] - salary invalid: {}", emCustInfoOutput.getSix_months_salary_payments());
                // ResponseCode + AGE_NOT_VALID
                result = new SimpleResult("SALARY AMOUNT INVALID", false, ResponseCode.IDTYPNO_NOT_VALID.getDesc());

                processContext.setResult(result);
                return true;
            }


        } catch (Exception e) {
            log.info("[SA INIT - vailate cust info - requestId:{} , desc:{} ", commonServiceRequest.getRequestId(), JSON.stringify(e));
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false, ResponseCode.TRANSACTION_FAIL.getDesc());

        }

        processContext.setResult(result);
        return !result.isOk();
    }

}
