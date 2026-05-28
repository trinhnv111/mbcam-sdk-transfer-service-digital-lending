package com.mbc.mobileapp.utils;

import org.apache.cxf.rs.security.jose.jwa.ContentAlgorithm;
import org.apache.cxf.rs.security.jose.jwa.KeyAlgorithm;
import org.apache.cxf.rs.security.jose.jwa.SignatureAlgorithm;
import org.apache.cxf.rs.security.jose.jwe.*;
import org.apache.cxf.rs.security.jose.jws.JwsCompactConsumer;
import org.apache.cxf.rs.security.jose.jws.JwsCompactProducer;
import org.apache.cxf.rs.security.jose.jws.JwsHeaders;
import org.apache.cxf.rs.security.jose.jws.PrivateKeyJwsSignatureProvider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.LinkedList;
import java.util.UUID;

public class JoseUtils {
    private static final String MBC_UUID = "MBC-UUID";
    private static final String MBC_TIMESTAMP = "MBC-TIMESTAMP";
    private static final String MBC_APPID = "MBC-APPID";

    public static String genJws(String message, String kid, String appId, PrivateKey privateK) throws Exception {
        return genJws(message, kid, null, appId, privateK);
    }

    public static String genJws(String message, String kid, String uuid, String appId, PrivateKey privateK) throws Exception {
        JwsHeaders jwsHeaders = new JwsHeaders(SignatureAlgorithm.RS256);
        if(!org.apache.commons.lang3.StringUtils.isEmpty(kid))
            jwsHeaders.setKeyId(kid);
        LinkedList<String> crit = new LinkedList<>();
        crit.add(MBC_UUID);
//        crit.add(MBC_TIMESTAMP);
        crit.add(MBC_APPID);

        jwsHeaders.setCritical(crit);
        if (StringUtils.isEmpty(uuid)) {
            jwsHeaders.setHeader(MBC_UUID, UUID.randomUUID().toString().replaceAll("-", ""));
        } else {
            jwsHeaders.setHeader(MBC_UUID, uuid);
        }

//        jwsHeaders.setHeader(MBC_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
        jwsHeaders.setHeader(MBC_APPID, appId);

        JwsCompactProducer jwsProducer = new JwsCompactProducer(jwsHeaders, message);
        jwsProducer.signWith(new PrivateKeyJwsSignatureProvider(privateK, SignatureAlgorithm.RS256));
        return jwsProducer.getSignedEncodedJws();
    }

    public static boolean parseJws(String jwsStr, PublicKey publicK) throws Exception {
        JwsCompactConsumer jwsCompactConsumer = new JwsCompactConsumer(jwsStr);
        return jwsCompactConsumer.verifySignatureWith(publicK, SignatureAlgorithm.RS256);
    }

    public static String genJwe(String content, PublicKey publicK, String kid) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        RSAKeyEncryptionAlgorithm rsaKeyEncryptionAlgorithm = new RSAKeyEncryptionAlgorithm((RSAPublicKey)publicK, KeyAlgorithm.RSA1_5, false);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        byte[] cek = uuid.getBytes(StandardCharsets.UTF_8);
        byte[] iv = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16).getBytes(StandardCharsets.UTF_8);
        JweEncryptionProvider encryptor = new AesCbcHmacJweEncryption(ContentAlgorithm.A128CBC_HS256, cek, iv, rsaKeyEncryptionAlgorithm);
        JweHeaders jweHeaders = new JweHeaders(KeyAlgorithm.RSA1_5, ContentAlgorithm.A128CBC_HS256);
        jweHeaders.setKeyId(kid);
        String jweContent = encryptor.encrypt(content.getBytes("UTF-8"), jweHeaders);
        return jweContent;
    }

    public static String parseJwe(String jweContent, PrivateKey privateK) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        RSAKeyDecryptionAlgorithm keyDecryptionAlgorithm = new RSAKeyDecryptionAlgorithm((RSAPrivateKey)privateK, KeyAlgorithm.RSA1_5, false);
        JweDecryptionProvider decryptor = new AesCbcHmacJweDecryption(keyDecryptionAlgorithm, ContentAlgorithm.A128CBC_HS256);
        JweDecryptionOutput decrypt = decryptor.decrypt(jweContent);
        String contentText = decrypt.getContentText();
        return contentText;
    }
}
