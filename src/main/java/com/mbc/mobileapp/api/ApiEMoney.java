package com.mbc.mobileapp.api;

import com.mbc.common.api.ApiBase;
import com.mbc.common.api.CallMicroService;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.salary_advance.input.EmCustInfoInput;
import com.mbc.mobileapp.api.model.salary_advance.output.*;
import com.mbc.mobileapp.utils.EmoneyApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 *   emoney.digital-lending.base-url
 *   emoney.digital-lending.merchant-code
 *   emoney.digital-lending.api-key
 */
@Slf4j
@Service
public class ApiEMoney extends CallMicroService {

    @Autowired
    private EmoneyApiUtil emoneyApiUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${emoney.digital-lending.base-url:}")
    private String baseUrl;

    @Value("${emoney.digital-lending.merchant-code:}")
    private String merchantCode;

    @Value("${emoney.digital-lending.api-key:}")
    private String apiKey;

    @Value("${emoney.digital-lending.mock-cust-info:false}")
    private boolean mockCustInfo;

    @PostConstruct
    public void init() {
        AppLog.info("[ApiEMoney] init - baseUrl: " + baseUrl + ", merchantCode: " + merchantCode
                + ", restTemplate: " + (restTemplate != null ? "OK" : "NULL"));
    }

    /**
     * API 1: customer/info — Lấy thông tin khách hàng
     */
//    public ExcuteEmoney<EmCustInfoData> getCustomerInfo(String msisdn, String idNumber, String custId, String requestId) {
//        try {
//            String url = baseUrl + "/" + merchantCode + "/digital-lending/customer/info";
//            AppLog.info("[API-EMONEY] customer/info - requestId:" + requestId + ", url:" + url + ", msisdn:" + msisdn);
//
//            String encrypted = emoneyApiUtil.encryptCustomerInfo(msisdn, idNumber);
//
//            EmCustInfoInput input = EmCustInfoInput.builder()
//                    .encrypt(encrypted)
//                    .build();
//
//            HttpHeaders headers = emoneyApiUtil.buildEmoneyHeaders(custId, requestId);
////            HttpEntity<EmCustInfoInput> requestEntity = new HttpEntity<>(input, headers);
//
//            ExcuteEmoney<EmCustInfoData> response = postForMicroService(
//                    url,
//                    headers,
//                    input,
//                    new ParameterizedTypeReference<ExcuteEmoney<EmCustInfoData>>() {}
//            );
////
////            ExcuteEmoney<EmCustInfoData> body = response.getBody();
////            AppLog.info("[API-EMONEY] customer/info response - requestId:" + requestId
////                    + ", status:" + (body != null ? body.getStatus() : "null")
////                    + ", code:" + (body != null ? body.getCode() : "null"));
//
//            return response;
//
//        } catch (Exception e) {
//            AppLog.error("[API-EMONEY] customer/info Exception - requestId:" + requestId + " - " + e.getMessage(), e);
//            return null;
//        }
//    }


    public ExcuteEmoney<EmCustInfoData> getCustomerInfo(String msisdn, String idNumber, String custId, String requestId) {
        // DEV ONLY: trả mock để bypass proxy khi test local
        if (mockCustInfo) {
            AppLog.info("[API-EMONEY] customer/info MOCK - requestId:" + requestId + ", msisdn:" + msisdn);
            return buildMockCustomerInfo();
        }

        try {
            String url = baseUrl + "/" + merchantCode + "/digital-lending/customer/info";
            AppLog.info("[API-EMONEY] customer/info - requestId:" + requestId + ", url:" + url + ", msisdn:" + msisdn);

            String encrypted = emoneyApiUtil.encryptCustomerInfo(msisdn, idNumber);

            EmCustInfoInput input = EmCustInfoInput.builder()
                    .encrypt(encrypted)
                    .build();

            // Build header đúng: Authorization + info-log (bắt buộc cho RestApiClientLogInterceptor)
            HttpHeaders headers = emoneyApiUtil.buildEmoneyHeaders(custId, requestId);
            HttpEntity<EmCustInfoInput> requestEntity = new HttpEntity<>(input, headers);

            ResponseEntity<ExcuteEmoney<EmCustInfoData>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<ExcuteEmoney<EmCustInfoData>>() {}
            );

            ExcuteEmoney<EmCustInfoData> body = response.getBody();
            AppLog.info("[API-EMONEY] customer/info response - requestId:" + requestId
                    + ", status:" + (body != null ? body.getStatus() : "null")
                    + ", code:" + (body != null ? body.getCode() : "null"));

            return body;

        } catch (Exception e) {
            AppLog.error("[API-EMONEY] customer/info Exception - requestId:" + requestId + " - " + e.getMessage(), e);
            return null;
        }
    }

    private ExcuteEmoney<EmCustInfoData> buildMockCustomerInfo() {
        EmCustomerInfo customerInfo = EmCustomerInfo.builder()
                .customerId("1123146")
                .familyName("Nguyen")
                .firstName("Van A")
                .englishName(false)
                .idType("NATIONAL_ID")
                .idNumber("155356878")
                .idExpiredDate("2030-01-15")
                .gender("MALE")
                .nationality("KH")
                .dateOfBirth("1995-06-20")
                .residential("Village 1, Phnom Penh Thmei, Phnom Penh")
                .phoneNumber("855123456789")
                .companyName("ABC Company Ltd")
                .currentOccupation("Employee")
                .work("Village 2, Tonle Bassac, Chamkarmon")
                .build();

        EmSalaryInfo salaryInfo = EmSalaryInfo.builder()
                .walletAgeDays(720)
                .kycLevel(1)
                .continuousSalary6Months(true)
                .salary3mAvgUSD(new BigDecimal("500.00"))
                .salary3mAvgKHR(new BigDecimal("2050000.00"))
                .salary3mMinUSD(new BigDecimal("500.00"))
                .salary3mMinKHR(new BigDecimal("1980000.00"))
                .salary3mMaxUSD(new BigDecimal("500.00"))
                .salary3mMaxKHR(new BigDecimal("2100000.00"))
                .salaryAmountT1USD(new BigDecimal("500.00"))
                .salaryAmountT1KHR(new BigDecimal("2100000.00"))
                .salaryAmountT2USD(new BigDecimal("500.00"))
                .salaryAmountT2KHR(new BigDecimal("2050000.00"))
                .salaryAmountT3USD(new BigDecimal("500.00"))
                .salaryAmountT3KHR(new BigDecimal("1980000.00"))
                .salaryCountT1(1)
                .salaryCountT2(1)
                .salaryCountT3(1)
                .build();

        EmWalletBehaviorInfo walletBehaviorInfo = EmWalletBehaviorInfo.builder()
                .walletId("245235")
                .utilityPaymentFlag(true)
                .utilityPaymentAmount(new BigDecimal("150000.00"))
                .telcoTopupFlag(true)
                .telcoTopupAvg(new BigDecimal("50000.00"))
                .gamblingCryptoFlag(false)
                .loanRepaymentHistory("ON_TIME")
                .interrupted6MonthsSalaryPayments("NO")
                .walletNumber(2)
                .topUpWallet(4)
                .topUpWalletAvg(new BigDecimal("500000.00"))
                .tranfersInWalletNum(3)
                .tranfersInWalletFreq(5)
                .tranfersInWalletAvg(new BigDecimal("200000.00"))
                .tranfersOutWalletNum(2)
                .tranfersOutWalletFreq(4)
                .tranfersOutWalletAvg(new BigDecimal("180000.00"))
                .avgWalletBalance(new BigDecimal("1200000.00"))
                .build();

        EmCustInfoData data = new EmCustInfoData(customerInfo, salaryInfo, walletBehaviorInfo);
        return new ExcuteEmoney<>(0, "MSG_SUCCESS", "Success", data);
    }



}
