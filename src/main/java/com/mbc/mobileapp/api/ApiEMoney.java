package com.mbc.mobileapp.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.api.CallApiGee;
import com.mbc.common.util.AppLog;
import com.mbc.mobileapp.api.model.salary_advance.input.EmCustInfoInput;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoData;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoResponse;
import com.mbc.mobileapp.api.model.salary_advance.output.ExcuteEmoney;
import com.mbc.mobileapp.utils.EmoneyApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;

/**
 * Gọi eMoney Digital Lending API qua Apigee gateway.
 * Kế thừa CallApiGee — tự động lấy Bearer token từ AccessToken (auth core)
 * và gắn vào header trước khi gọi Apigee.
 *
 * Config (application.properties):
 *   apigee.host.url                              - Apigee gateway host
 *   apigee.emoney.digital-lending.customer-info.url  - path customer/info qua Apigee
 */
@Slf4j
@Service
public class ApiEMoney extends CallApiGee {

    @Autowired
    private EmoneyApiUtil emoneyApiUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${apigee.emoney.digital-lending.customer-info.url:}")
    private String apigeeEmoneyPath;

    @Value("${apigee.emoney.digital-lending.token:}")
    private String emoneyToken;

    @PostConstruct
    public void initApiEMoney() {
        AppLog.info("[ApiEMoney] init - apigeeHost: " + getUrl("apigee.host.url")
                + ", apigeeEmoneyPath: " + apigeeEmoneyPath);
    }

    /**
     * Lấy thông tin khách hàng từ eMoney qua Apigee.
     * URL: apigee.host.url + apigee.emoney.digital-lending.cust-info.url
     */
    public ExcuteEmoney<EmCustInfoData> getCustomerInfo(String msisdn, String idNumber, String custId, String requestId) {
        try {
            String fullUrl = getUrl("apigee.host.url") + apigeeEmoneyPath;
            AppLog.info("[API-EMONEY] customer/info via Apigee - requestId:" + requestId + ", url:" + fullUrl);

            String encrypted = emoneyApiUtil.encryptCustomerInfo(msisdn, idNumber);
            EmCustInfoInput input = EmCustInfoInput.builder()
                    .encrypt(encrypted)
                    .build();

            HttpHeaders headers = buildHeader(custId, requestId, requestId);
            headers.add("PARTNER", "EMONEY");
            headers.add("emoney-token", emoneyToken);

            EmCustInfoResponse response = postForApigee(
                    UriComponentsBuilder.fromHttpUrl(fullUrl).build().toUri(),
                    headers,
                    input,
                    EmCustInfoResponse.class
            );

            if (response == null) {
                AppLog.error("[API-EMONEY] customer/info null response - requestId:" + requestId, null);
                return new ExcuteEmoney<>(504, "TIMEOUT", "Apigee response is null", null);
            }

            String errorCode = response.getErrorCode();
            if (errorCode != null && !errorCode.isEmpty() && !"00".equals(errorCode) && !"0".equals(errorCode)) {
                AppLog.error("[API-EMONEY] customer/info Apigee error - requestId:" + requestId
                        + ", errorCode:" + errorCode
                        + ", errorDesc:" + response.getErrorDesc(), null);
                return new ExcuteEmoney<>(500, errorCode, String.valueOf(response.getErrorDesc()), null);
            }
            // Apigee wraps eMoney response inside response.data:
            // response.data = { "status":0, "code":"MSG_SUCCESS", "message":"Success",
            //                   "data": { salaryInfo:{...}, customerInfo:{...} } }
            JsonNode outerData = response.getData();
            if (outerData == null) {
                AppLog.error("[API-EMONEY] customer/info outer data null - requestId:" + requestId, null);
                return new ExcuteEmoney<>(500, "DATA_NULL", "Apigee outer data is null", null);
            }

            // Parse the eMoney wrapper layer: {status, code, message, data}
            ExcuteEmoney<JsonNode> emoneyWrapper = objectMapper.convertValue(
                    outerData, new TypeReference<ExcuteEmoney<JsonNode>>() {});

            Integer status = emoneyWrapper.getStatus();
            String code = emoneyWrapper.getCode();
            String message = emoneyWrapper.getMessage();

            AppLog.info("[API-EMONEY] customer/info response - requestId:" + requestId
                    + ", status:" + status
                    + ", code:" + code);

            // Parse the actual EmCustInfoData from inner data
            EmCustInfoData data = null;
            if (emoneyWrapper.getData() != null) {
                data = objectMapper.convertValue(emoneyWrapper.getData(), EmCustInfoData.class);
            }

            return new ExcuteEmoney<>(status, code, message, data);

        } catch (Exception e) {
            AppLog.error("[API-EMONEY] customer/info Exception - requestId:" + requestId + " - " + e.getMessage(), e);
            return new ExcuteEmoney<>(500, "EXCEPTION", e.getMessage(), null);
        }
    }
}