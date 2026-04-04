package com.mbc.mobileapp.authen.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncryptedRequest {
    @NotBlank(message = "nonce is mandatory")
    @Length(min = 12, message = "nonce length is invalid")
    private String nonce;

    @NotBlank(message = "cipherText is mandatory")
    @Length(min = 16, message = "cipherText length is invalid")
    private String cipherText;

    private String aad;
}
