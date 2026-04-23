package com.mbc.mobileapp.utils;

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
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * Utility cho eMoney Digital Lending API
 * - Build HTTP headers (Authorization: epa <API_KEY>)
 * - RSA encrypt (load public key từ classpath .pem)
 *
 * Config (application.properties):
 *   emoney.digital-lending.api-key
 *   emoney.digital-lending.public-key-path
 */
@Slf4j
@Component
public class EmoneyApiUtil {

    @Value("${emoney.digital-lending.api-key:}")
    private String apiKey;

    @Value("${emoney.digital-lending.public-key-path:rsakey/dev/public_key.pem}")
    private String publicKeyPath;

    private PublicKey rsaPublicKey;

    @PostConstruct
    public void init() {
        this.rsaPublicKey = loadPublicKey(publicKeyPath);
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
        String plainText = msisdn + "|" + idNumber;
        return rsaEncrypt(plainText);
    }

    /**
     * Encrypt plaintext bằng RSA public key (PKCS1Padding)
     */
    public String rsaEncrypt(String plainText) {
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
     * Load public key từ classpath resource (.pem)
     */
    private PublicKey loadPublicKey(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String keyContent;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream()))) {
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
            log.info("[EmoneyApiUtil] Loaded public key from: {}", path);
            return key;
        } catch (Exception e) {
            log.error("[EmoneyApiUtil] Failed to load public key from: {}", path, e);
            return null;
        }
    }
}
