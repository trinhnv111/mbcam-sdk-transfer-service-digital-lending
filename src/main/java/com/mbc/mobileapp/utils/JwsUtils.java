package com.mbc.mobileapp.utils;

import com.mbc.mobileapp.config.ErrorCode;
import com.mbc.mobileapp.exception.BusinessException;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.common.util.Base64UrlUtility;
import org.apache.cxf.rs.security.jose.jwe.JweCompactConsumer;
import org.apache.cxf.rs.security.jose.jws.JwsCompactConsumer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwsUtils {
    private final KeyManagementService keyManagementService;

    public static String packageJws(String detachedJwsContent, String payload) {
        String[] parts = getCompactParts(detachedJwsContent);
        String payloadBase64Url = Base64UrlUtility.encode(payload);
        String jwsContent = parts[0] + "." + payloadBase64Url + "." + parts[2];
        return jwsContent;
    }

    public static String detachedJwsContent(String jwsContent) {
        String[] parts = getCompactParts(jwsContent);
        String detachedJwsContent = parts[0] + "." + "." + parts[2];
        return detachedJwsContent;
    }

    public static String[] getCompactParts(String compactContent) {
        if (compactContent.startsWith("\"") && compactContent.endsWith("\"")) {
            compactContent = compactContent.substring(1, compactContent.length() - 1);
        }

        return StringUtils.split(compactContent, "\\.");
    }

    public static String getJwsKid(String jwsContent) {
        JwsCompactConsumer jwsCompactConsumer = new JwsCompactConsumer(jwsContent);
        return jwsCompactConsumer.getJwsHeaders().getKeyId();
    }

    public static String getJweKid(String jweContent) {
        JweCompactConsumer consumer = new JweCompactConsumer(jweContent);
        return consumer.getJweHeaders().getKeyId();
    }

    public static String getJwsUuid(String jwsContent) {
        JwsCompactConsumer jwsCompactConsumer = new JwsCompactConsumer(jwsContent);
        return (String) jwsCompactConsumer.getJwsHeaders().getHeader("MBC-UUID");
    }

    public String generateJWSContent(String partnerCode, String payload) {
        String detachedJwsContent = "";
        try {
            // Get publicKey Info
            Tuple3<String, String, PublicKey> publicKeyInfo = extractCertPublicKeyString(keyManagementService.getPublicKeyString(partnerCode));
//            log.info("CertPublicKey generate signature JWS: {}", publicKeyInfo._2);
            // Get privateKey Info
            Tuple2<String, PrivateKey> privateKeyInfo = extractPrivateKeyString(keyManagementService.getPrivateKeyString(partnerCode));
//            log.info("PrivateKey generate signature JWS: {}", privateKeyInfo._1);

            //Generate JWS information
            String jwsContent = JoseUtils.genJws(payload, publicKeyInfo._1, partnerCode, privateKeyInfo._2);
            //Detached payload part,the value of detachedJwsContent can be used to the http header "MBC-JWS"
            detachedJwsContent = JwsUtils.detachedJwsContent(jwsContent);
        } catch (Exception e) {
            log.error("genJws error: ", e);
            throw new BusinessException(ErrorCode.CERT_INVALID);
        }
        return detachedJwsContent;
    }

    public String generateJWSContent(String partnerCode, String payload, String privateKey, String publicKey) {
        String detachedJwsContent = "";
        try {
            // Get publicKey Info
            Tuple3<String, String, PublicKey> publicKeyInfo = extractCertPublicKeyString(publicKey);
//            log.info("CertPublicKey generate signature JWS: {}", publicKeyInfo._2);
            // Get privateKey Info
            Tuple2<String, PrivateKey> privateKeyInfo = extractPrivateKeyString(privateKey);
//            log.info("PrivateKey generate signature JWS: {}", privateKeyInfo._1);

            //Generate JWS information
            String jwsContent = JoseUtils.genJws(payload, publicKeyInfo._1, partnerCode, privateKeyInfo._2);
            //Detached payload part,the value of detachedJwsContent can be used to the http header "MBC-JWS"
            detachedJwsContent = JwsUtils.detachedJwsContent(jwsContent);
        } catch (Exception e) {
            log.error("genJws error: ", e);
            throw new BusinessException(ErrorCode.CERT_INVALID);
        }
        return detachedJwsContent;
    }

    public boolean verifyJWSContent(String partnerCode, String detachedJwsContent, String payload) {
        try {
            // Get publicKey Info
            Tuple3<String, String, PublicKey> publicKeyInfo = extractCertPublicKeyString(keyManagementService.getPublicKeyString(partnerCode));
//            log.info("CertPublicKey verify signature JWS: {}", publicKeyInfo._2);

            //Combine the detachedJwsContent part and payload part into jwsContent
            String jwsContent = JwsUtils.packageJws(detachedJwsContent, payload);

            //Verify the value of jwsContent
            return JoseUtils.parseJws(jwsContent, publicKeyInfo._3);
        } catch (Exception e) {
            log.error("parseJws error: ", e);
            return false;
        }
    }

    public static Tuple3<String, String, PublicKey> extractCertPublicKeyString(String cert) {
        String kid;
        PublicKey publicKey;
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            byte[] certificateData = Base64.getDecoder().decode(cert.getBytes(StandardCharsets.UTF_8));
            X509Certificate certInstance = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certificateData));
            certInstance.checkValidity();
            kid = certInstance.getSerialNumber().toString();
            publicKey = certInstance.getPublicKey();
        } catch (Exception e) {
            log.error("Exception | CertService: ", e);
            throw new BusinessException(ErrorCode.CERT_ERROR);
        }
        return Tuple.of(kid, cert, publicKey);
    }

    public static Tuple2<String, PrivateKey> extractPrivateKeyString(String privateKeyString) {
        return Tuple.of(privateKeyString, getPrivateKeyObjectFromPrivateKeyString(privateKeyString));
    }

    private static PrivateKey getPrivateKeyObjectFromPrivateKeyString(String privateKey) {
        //The format of the Private key string must be PKCS8
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory privateKeyFactory;
        try {
            privateKeyFactory = KeyFactory.getInstance("RSA");
            return privateKeyFactory.generatePrivate(pkcs8KeySpec);
        } catch (Exception e) {
            log.error("privateKey get error: ", e);
            throw new BusinessException(ErrorCode.CERT_ERROR);
        }
    }

    public String generateJWEContent(String partnerCode, String payload) {
        String jweContent = "";
        try {
            // Get publicKey Info
            Tuple3<String, String, PublicKey> publicKeyInfo = extractCertPublicKeyString(keyManagementService.getPublicKeyString(partnerCode));
//            log.info("CertPublicKey generate encrypt JWE: {}", publicKeyInfo._2);

            jweContent = JoseUtils.genJwe(payload, publicKeyInfo._3, publicKeyInfo._1);
        } catch (Exception e) {
            log.error("genJwe error: ", e);
            throw new BusinessException(ErrorCode.CERT_INVALID);
        }
        return jweContent;
    }

    public String parseJWEContent(String jweContent, String partnerCode) {
        try {
            // Get privateKey Info
            Tuple2<String, PrivateKey> privateKeyInfo = extractPrivateKeyString(keyManagementService.getPrivateKeyString(partnerCode));
//            log.info("PrivateKey parse encrypt JWE: {}", privateKeyInfo._1);

            // Parse the jweContent
            return JoseUtils.parseJwe(jweContent, privateKeyInfo._2);
        } catch (Exception e) {
            log.error("parseJwe error: ", e);
            throw new BusinessException(ErrorCode.CERT_INVALID);
        }
    }
}

