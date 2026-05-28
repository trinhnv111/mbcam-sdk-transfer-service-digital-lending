package com.mbc.mobileapp.utils;

import com.mbc.common.util.JSON;
import com.mbc.mobileapp.authen.config.EncryptedRequest;
import com.mbc.mobileapp.config.ErrorCode;
import com.mbc.mobileapp.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AeadUtils {

    private static final int CHACHA20_KEY_SIZE = 32;
    private static final int CHACHA20_NONCE_SIZE = 12;
    private static final int POLY1305_TAG_SIZE = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public EncryptedRequest encryptForClient(byte[] key, byte[] plaintext, byte[] aad) {
        if (key.length != 32) {
            throw new IllegalArgumentException("Key must be exactly 32 bytes");
        }

        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext cannot be null");
        }

        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);

        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce, aad);
        cipher.init(true, params); // true for encryption

        byte[] ciphertext = new byte[cipher.getOutputSize(plaintext.length)];
        int len = cipher.processBytes(plaintext, 0, plaintext.length, ciphertext, 0);

        try {
            cipher.doFinal(ciphertext, len);
        } catch (Exception e) {
            log.error("[AeadUtils] encryptForClient error {}", e);
            throw new RuntimeException("Encryption failed", e);
        }
        EncryptedRequest request = EncryptedRequest.builder()
                .aad(Base64.getEncoder().encodeToString(aad))
                .cipherText(Base64.getEncoder().encodeToString(ciphertext))
                .nonce(Base64.getEncoder().encodeToString(nonce))
                .build();
        log.info("[AeadUtils] encryptForClient data {}", JSON.stringify(request));
        return request;
    }

    public String decryptFromClient(byte[] key, byte[] ciphertext, byte[] nonce, byte[] aad) {

        ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
        AEADParameters params = new AEADParameters(new KeyParameter(key), 128, nonce, aad);
        cipher.init(false, params);

        byte[] output = new byte[cipher.getOutputSize(ciphertext.length)];
        int len = cipher.processBytes(ciphertext, 0, ciphertext.length, output, 0);

        try {
            cipher.doFinal(output, len);
        } catch (Exception e) {
            log.error("[AEADUtils] error Invalid ciphertext or tag {}", e);
            throw new BusinessException(ErrorCode.MESSAGE_FORMAT_ERROR);
        }

        return new String(output, StandardCharsets.UTF_8);
    }


    public String getKey(String pathFile) throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(pathFile);
        StringBuilder privKeyBytes = new StringBuilder();
        if (inputStream == null) {
            throw new RuntimeException("resource not found");
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String END;
            while ((END = reader.readLine()) != null) {
                privKeyBytes.append(END);
            }
            String BEGIN = "-----BEGIN KEY-----";
            END = "-----END KEY-----";

            String str = new String(privKeyBytes);
            if (str.contains(BEGIN) && str.contains(END)) {
                str = str.substring(BEGIN.length(), str.lastIndexOf(END));
            }
            return str;
        }
    }

}
