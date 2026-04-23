package com.mbc.mobileapp.api;

import com.mbc.common.api.ApiBase;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.JSON;
import com.mbc.mobileapp.api.model.salary_advance.input.EmCustInfoInput;
import com.mbc.mobileapp.api.model.salary_advance.output.EmBaseResponse;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * API Client cho eMoney Digital Lending
 *
 * Auth: Authorization: epa <API_KEY>
 * Request: RSA encrypt (msisdn|idNumber) bằng public key
 *
 * Config (application.properties):
 *   emoney.digital-lending.base-url   = https://payment.emoney.com.kh:8301
 *   emoney.digital-lending.merchant-code = MBCLENDING
 *   emoney.digital-lending.api-key    = <api key>
 *   emoney.digital-lending.public-key-path = rsakey/dev/public_key.pem
 */
@Slf4j
@Service
public class ApiDigitalLending extends ApiBase {

    private String baseUrl;
    private String merchantCode;
    private String apiKey;
    private PublicKey rsaPublicKey;

    @PostConstruct
    public void init() {
        this.baseUrl = getUrl("emoney.digital-lending.base-url");
        this.merchantCode = getUrl("emoney.digital-lending.merchant-code");
        this.apiKey = getUrl("emoney.digital-lending.api-key");
        String publicKeyPath = getUrl("emoney.digital-lending.public-key-path");
        this.rsaPublicKey = loadPublicKey(publicKeyPath);
    }

    /**
     * API 1: customer/info — Lấy thông tin khách hàng
     * POST /{merchantCode}/digital-lending/customer/info
     */
    public EmBaseResponse<EmCustInfoData> getCustomerInfo(String msisdn, String idNumber, String requestId) {
        try {
            String url = baseUrl + "/" + merchantCode + "/digital-lending/customer/info";
            String plainText = msisdn + "|" + idNumber;
            String encrypted = rsaEncrypt(plainText);

            EmCustInfoInput input = EmCustInfoInput.builder()
                    .encrypt(encrypted)
                    .build();

            HttpHeaders headers = buildEmoneyHeaders(requestId);
            HttpEntity<EmCustInfoInput> requestEntity = new HttpEntity<>(input, headers);

            AppLog.info("[API-EMONEY] customer/info - requestId:" + requestId + ", url:" + url);

            ResponseEntity<EmBaseResponse<EmCustInfoData>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<EmBaseResponse<EmCustInfoData>>() {}
            );

            EmBaseResponse<EmCustInfoData> body = response.getBody();
            AppLog.info("[API-EMONEY] customer/info response - requestId:" + requestId
                    + ", status:" + (body != null ? body.getStatus() : "null")
                    + ", code:" + (body != null ? body.getCode() : "null"));

            return body;

        } catch (Exception e) {
            AppLog.error("[API-EMONEY] customer/info Exception - requestId:" + requestId, e);
            return null;
        }
    }

    /**
     * Build HTTP headers cho eMoney API
     * Authorization: epa <API_KEY>
     */
    private HttpHeaders buildEmoneyHeaders(String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "epa " + apiKey);
        return headers;
    }

    /**
     * RSA encrypt bằng public key
     */
    private String rsaEncrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            AppLog.error("[API-EMONEY] RSA encrypt failed", e);
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

    /**
     * Load public key từ resource path (classpath)
     */
    private PublicKey loadPublicKey(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String keyContent;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                keyContent = reader.lines().collect(Collectors.joining());
            }

            keyContent = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(keySpec);
            AppLog.info("[API-EMONEY] Loaded public key from: " + path);
            return key;
        } catch (Exception e) {
            AppLog.error("[API-EMONEY] Failed to load public key from: " + path, e);
            return null;
        }
    }
}
