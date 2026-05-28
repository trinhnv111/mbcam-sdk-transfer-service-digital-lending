package com.mbc.mobileapp.utils;

import com.mbc.common.repository.ComPartnerSdkRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeyManagementService {
    private final ComPartnerSdkRepo partnerSdkRepo;

    public String getPublicKeyString(String partnerCode) {
        return partnerSdkRepo.findByPartnerCode(partnerCode).orElse(null).getPublicKey();
    }

    public String getPrivateKeyString(String partnerCode) {
        return partnerSdkRepo.findByPartnerCode(partnerCode).orElse(null).getPrivateKey();
    }

    public Optional<PrivateKey> getPrivateKey(String partnerCode) {
        return partnerSdkRepo.findByPartnerCode(partnerCode)
                .map(key -> this.stringToPrivateKey(key.getPrivateKey()));
    }

    public Optional<PublicKey> getPublicKey(String partnerCode) {
        return partnerSdkRepo.findByPartnerCode(partnerCode)
                .map(key -> this.stringToPublicKey(key.getPublicKey()));
    }

    public  PublicKey stringToPublicKey(String publicKeyString) {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    public  PrivateKey stringToPrivateKey(String privateKeyString) {
        try {
            String cleanedKey = privateKeyString
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] privateKeyBytes = Base64.getDecoder().decode(cleanedKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

}
