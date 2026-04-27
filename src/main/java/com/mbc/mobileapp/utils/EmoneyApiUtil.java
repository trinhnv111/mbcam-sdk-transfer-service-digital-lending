package com.mbc.mobileapp.utils;

import com.mbc.common.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility cho eMoney Digital Lending API
 * - Build HTTP headers (Authorization: epa <API_KEY>)
 * - RSA encrypt (load public key từ classpath .pem)
 *
 * Config (application.properties):
 *   emoney.digital-lending.api-key
 *   emoney.digital-lending.public-key-path  (relative to classpath, default: pubic_key.pem)
 */
@Slf4j
@Component
public class EmoneyApiUtil {

    @Value("${emoney.digital-lending.api-key:}")
    private String apiKey;

    // Mặc định dùng pubic_key.pem nằm thẳng trong src/main/resources
    @Value("${emoney.digital-lending.public-key-path:pubic_key.pem}")
    private String publicKeyPath;

    private PublicKey rsaPublicKey;

    @PostConstruct
    public void init() {
        this.rsaPublicKey = loadPublicKey(publicKeyPath);
        if (this.rsaPublicKey == null) {
            log.error("[EmoneyApiUtil] WARN: rsaPublicKey is NULL — encrypt sẽ bị fail khi gọi eMoney! Kiểm tra lại path: {}", publicKeyPath);
        } else {
            log.info("[EmoneyApiUtil] RSA public key loaded OK from: {}", publicKeyPath);
        }
    }

    /**
     * Build HTTP headers cho eMoney API
     * Authorization: epa <API_KEY>
     */
    public HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "epa " + apiKey);
        return headers;
    }

    /**
     * Build encrypted string cho eMoney API customer/info
     * Format: RSA(msisdn|idNumber)
     */
    public String encryptCustomerInfo(String msisdn, String idNumber) {
        log.info("[EmoneyApiUtil] encryptCustomerInfo - msisdn: {}", msisdn);
        String plainText = msisdn + "|" + idNumber;
        return rsaEncrypt(plainText);
    }

    /**
     * Encrypt plaintext bằng RSA public key (PKCS1Padding)
     */
    public String rsaEncrypt(String plainText) {
        if (rsaPublicKey == null) {
            throw new RuntimeException("[EmoneyApiUtil] rsaPublicKey is null, cannot encrypt. Kiểm tra file: " + publicKeyPath);
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("[EmoneyApiUtil] RSA encrypt failed", e);
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

    /**
     * Load RSA public key từ classpath resource (.pem — X.509 / PKCS#8 format)
     */
    private PublicKey loadPublicKey(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                log.error("[EmoneyApiUtil] Public key file NOT FOUND in classpath: '{}'", path);
                return null;
            }


            String keyContent;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                keyContent = reader.lines().collect(Collectors.joining("\n"));
            }

            keyContent = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            if (keyContent.isEmpty()) {
                log.error("[EmoneyApiUtil] Public key content empty after stripping PEM headers: {}", path);
                return null;
            }

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(keySpec);
            log.info("[EmoneyApiUtil] Loaded RSA public key successfully from: {}", path);
            return key;
        } catch (Exception e) {
            log.error("[EmoneyApiUtil] Failed to load public key from: '{}' — {}", path, e.getMessage(), e);
            return null;
        }
    }


    public HttpHeaders buildEmoneyHeaders(String custId, String requestId) {
        String messageId = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "epa " + apiKey);
        headers.set("clientMessageId", messageId);
        List<String> infoLog = Arrays.asList(
                Utility.isNull(custId) ? "" : custId,
                Utility.isNull(requestId) ? "" : requestId,
                messageId
        );
        headers.put("info-log", infoLog);
        return headers;
    }
}
