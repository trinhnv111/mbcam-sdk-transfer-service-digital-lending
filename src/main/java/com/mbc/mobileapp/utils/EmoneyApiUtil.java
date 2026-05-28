package com.mbc.mobileapp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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
 * Utility cho eMoney Digital Lending API — RSA encrypt
 *
 * Config (application.properties):
 *   emoney.digital-lending.public-key-path  (relative to classpath, default: pubic_key.pem)
 */
@Slf4j
@Component
public class EmoneyApiUtil {

    @Value("${emoney.digital-lending.public-key-path}")
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

    public enum Padding {
        RSA("RSA"),
        OAEP("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"),
        PKCS1("RSA/ECB/PKCS1Padding");

        final String algorithm;

        Padding(String algorithm) {
            this.algorithm = algorithm;
        }
    }

    private static final Padding DEFAULT = Padding.RSA;

    /**
     * RSA(msisdn|idNumber) cho eMoney API customer/info
     */
    public String encryptCustomerInfo(String msisdn, String idNumber) {
        log.info("[EmoneyApiUtil] encryptCustomerInfo - msisdn: {}", msisdn);
        String plainText = msisdn + "|" + idNumber;
        return rsaEncrypt(plainText);
    }

    public String rsaEncrypt(String plainText) {
        if (rsaPublicKey == null) {
            throw new RuntimeException("[EmoneyApiUtil] rsaPublicKey is null, cannot encrypt. Kiểm tra file: " + publicKeyPath);
        }
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT.algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("[EmoneyApiUtil] RSA encrypt failed", e);
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

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

            return parsePublicKey(keyContent);
        } catch (Exception e) {
            log.error("[EmoneyApiUtil] Failed to load public key from: '{}' — {}", path, e.getMessage(), e);
            return null;
        }
    }

    public static PublicKey parsePublicKey(String base64) throws Exception {
        String clean = cleanBase64(base64, true);
        byte[] bytes = Base64.getDecoder().decode(clean);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static String cleanBase64(String base64, boolean isPublic) {
        return base64
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }
}
