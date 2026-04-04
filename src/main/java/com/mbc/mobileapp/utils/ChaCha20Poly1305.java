package com.mbc.mobileapp.utils;

import com.mbc.common.util.Utility;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class ChaCha20Poly1305 {
//    public static byte[] encrypt(byte[] data, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
//            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
//        if(key == null) throw new InvalidKeyException("SecretKey must NOT be NULL");
//
//        byte[] nonceBytes = new byte[12];
//
//        // Get Cipher Instance
//        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");
//
//        // Create IvParamterSpec
//        AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);
//
//        // Create SecretKeySpec
//        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "ChaCha20");
//
//        // Initialize Cipher for ENCRYPT_MODE
//        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
//
//        // Perform Encryption
//        return cipher.doFinal(data);
//    }
//
//    public static byte[] decrypt(byte[] cipherText, SecretKey key) throws Exception {
//        if(key == null) throw new InvalidKeyException("SecretKey must NOT be NULL");
//        byte[] nonceBytes = new byte[12];
//
//        // Get Cipher Instance
//        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding");
//
//        // Create IvParamterSpec
//        AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(nonceBytes);
//
//        // Create SecretKeySpec
//        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "ChaCha20");
//
//        // Initialize Cipher for DECRYPT_MODE
//        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
//
//        // Perform Decryption
//        return cipher.doFinal(cipherText);
//    }
//
//    public static SecretKey generateKey(String pass) throws NoSuchAlgorithmException {
//        KeyGenerator keyGenerator = KeyGenerator.getInstance("ChaCha20");
//        //Keysize MUST be 256 bit - as of Java11 only 256Bit is supported
//        keyGenerator.init(256);
//        return keyGenerator.generateKey();
//    }
//
//    public static SecretKey convertStringToSecretKeyto(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
//
//
//        byte[] salt = new SecureRandom().generateSeed(16);
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("ChaCha20-Poly1305");
//        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
//        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "ChaCha20");
//        return key;
//    }
}
