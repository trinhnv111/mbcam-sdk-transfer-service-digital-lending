package com.mbc.mobileapp.api.model.remittance.input;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemittanceAddressInput {
    
    private String code;
    private String type;

}
