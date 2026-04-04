package com.mbc.mobileapp.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncryptedMessage {
    public final byte[] nonce;
    public final byte[] ciphertext;
    public final byte[] aad;

    public EncryptedMessage(byte[] nonce, byte[] ciphertext, byte[] aad) {
        this.nonce = nonce;
        this.ciphertext = ciphertext;
        this.aad = aad;
    }
}
