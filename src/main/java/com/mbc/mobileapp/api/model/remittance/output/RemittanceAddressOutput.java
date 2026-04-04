package com.mbc.mobileapp.api.model.remittance.output;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemittanceAddressOutput {

    private String code;
    private String name;
}
