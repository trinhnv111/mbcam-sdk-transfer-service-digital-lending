package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.JSON;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j

public class DoGetCustInfoFromEM implements Command {

    // 1.gọi api từ em -> cust
    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result =Validator.Result.OK ;
        CommonServiceRequest commonServiceRequest =  (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = commonServiceRequest.getCust();

        try {
            log.info("[SA INIT - GET CUST FROM EM] - requestId:{} , cifId:{} ", commonServiceRequest.getRequestId(),custInfo.getHostCifId());
            String idNumber = custInfo.getIdTypNo();

            EmCustInfoOutput emCustInfoOutput = buidHardCustInfo(idNumber,custInfo);
            // ném vào trong processcntx
            processContext.put("emCustInfoOutput",emCustInfoOutput);
        }
        catch (Exception e ){
            log.info("[SA INIT - GET CUST FROM EM] Exception- requestId:{} , desc:{} ", commonServiceRequest.getRequestId(), JSON.stringify(e));
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(),false,ResponseCode.TRANSACTION_FAIL.getDesc());

        }

        ((ProcessContext) context).setResult(result);
        return !result.isOk();
    }

    // hardcode
    private EmCustInfoOutput buidHardCustInfo(String idNumber , CustInfo custInfo){
        return EmCustInfoOutput.builder()
                // Nhóm định danh
                .customerId(custInfo.getHostCifId())
                .familyName("TEST")
                .firstName("SALARY ADVANCE")
                .englishName("TEST SALARY ADVANCE")
                .idType("NATIONAL_ID")
                .idNumber(idNumber)  // Lấy từ session để match validate
                .idExpiredDate("2030-12-31")
                .gender("M")
                .maritalStatus("Single")
                .nationality("Cambodia")
                .dateOfBirth("1995-06-15")
                // Nơi sinh
                .placeOfBirthCountry("Cambodia")
                .placeOfBirthProvince("Phnom Penh")
                .placeOfBirthDistrict("Chamkarmon")
                .placeOfBirthCommune("Tonle Bassac")
                // Email
                .email("test.sa@mbc.com.kh")
                // Nơi cư trú
                .residentialCountry("Cambodia")
                .residentialProvince("Phnom Penh")
                .residentialDistrict("Chamkarmon")
                .residentialCommune("Tonle Bassac")
                .residentialVillage("Village 1")
                // Số điện thoại
                .phoneNumber("+855 12 345 678")
                // Thông tin việc làm
                .companyName("MBC Bank Cambodia")
                .currentOccupation("Staff")
                .employmentDate("2020-01-15")
                .occupationLengthService(48)


                .monthlySalaryAmountUsd(new BigDecimal("1500.00"))
                .six_months_salary_payments(true)
                // Nơi làm việc
                .workCountry("Cambodia")
                .workProvince("Phnom Penh")
                .workDistrict("Chamkarmon")
                .workCommune("Tonle Bassac")
                .workVillage("Village 1")

                .build();
    }


}
