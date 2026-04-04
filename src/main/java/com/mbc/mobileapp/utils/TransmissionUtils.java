package com.mbc.mobileapp.utils;

import com.mbc.mobileapp.authen.config.EncryptedRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class TransmissionUtils extends AeadUtils {
    public EncryptedRequest encrypt(String keyStr, String plaintextStr, String aadStr) {
        byte[] key;
        byte[] plaintext;
        byte[] aad = null;

        try {
            key = Base64.getDecoder().decode(keyStr);
            plaintext = plaintextStr.getBytes(StandardCharsets.UTF_8);

            if (aadStr != null && !aadStr.isEmpty()) {
                aad = Base64.getDecoder().decode(aadStr);
            }
            return super.encryptForClient(key, plaintext, aad);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encoding in key or AAD", e);
        }
    }

    public String decrypt(String keyStr, String cipherStr, String nonceStr, String aadStr) {
        byte[] key;
        byte[] cipherText;
        byte[] nonce;
        byte[] aad = null;

        try {
//            key = Base64.getDecoder().decode(keyStr);
            key = MessageDigest.getInstance("SHA-256")
                    .digest(keyStr.getBytes(StandardCharsets.UTF_8));
            cipherText = Base64.getDecoder().decode(cipherStr);
            nonce = Base64.getDecoder().decode(nonceStr);

            if (aadStr != null && !aadStr.isEmpty()) {
                aad = Base64.getDecoder().decode(aadStr);
            }
            return super.decryptFromClient(key, cipherText, nonce, aad);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Base64 encoding in input strings", e);
        }

    }

}
