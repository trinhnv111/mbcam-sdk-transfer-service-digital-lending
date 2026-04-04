package com.mbc.mobileapp.api.model.remittance.output;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemittanceDiscount {
    
    private String discountCode;
    
    private String discountAmount;
    
    private String discountReason;

}
