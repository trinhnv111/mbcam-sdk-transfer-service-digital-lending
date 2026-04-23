package com.mbc.mobileapp.api;

import com.mbc.common.api.ApiBase;
import com.mbc.common.util.AppLog;
import com.mbc.mobileapp.api.model.salary_advance.input.EmCustInfoInput;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoData;
import com.mbc.mobileapp.api.model.salary_advance.output.ExcuteEmoney;
import com.mbc.mobileapp.utils.EmoneyApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * API Client cho eMoney Digital Lending — chỉ call API
 * Crypto: EmoneyEncryptUtil (common)
 * Headers: EmoneyApiUtil (sdk-transfer/utils)
 *
 * Config:
 *   emoney.digital-lending.base-url
 *   emoney.digital-lending.merchant-code
 */
@Slf4j
@Service
public class ApiEMoney extends ApiBase {

    @Autowired
    private EmoneyApiUtil emoneyApiUtil;

    private String baseUrl;
    private String merchantCode;

    @PostConstruct
    public void init() {
        this.baseUrl = getUrl("emoney.digital-lending.base-url");
        this.merchantCode = getUrl("emoney.digital-lending.merchant-code");
    }

    /**
     * API 1: customer/info — Lấy thông tin khách hàng - /{merchantCode}/digital-lending/customer/info
     */
    public ExcuteEmoney<EmCustInfoData> getCustomerInfo(String msisdn, String idNumber, String requestId) {
        try {
            String url = baseUrl + "/" + merchantCode + "/digital-lending/customer/info";

            String encrypted = emoneyApiUtil.encryptCustomerInfo(msisdn, idNumber);

            EmCustInfoInput input = EmCustInfoInput.builder()
                    .encrypt(encrypted)
                    .build();

            HttpHeaders headers = emoneyApiUtil.buildHeaders();
            HttpEntity<EmCustInfoInput> requestEntity = new HttpEntity<>(input, headers);

            AppLog.info("[API-EMONEY] customer/info - requestId:" + requestId + ", url:" + url);

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
            AppLog.error("[API-EMONEY] customer/info Exception - requestId:" + requestId, e);
            return null;
        }
    }
}
