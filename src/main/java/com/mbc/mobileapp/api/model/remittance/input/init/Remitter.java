package com.mbc.mobileapp.api.model.remittance.input.init;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Remitter {
    private String account;
    private String name;
    private String phoneNumber;
    private String documentType;
    private String documentNum;
    private String address;
    private String custKycStatus;
}
