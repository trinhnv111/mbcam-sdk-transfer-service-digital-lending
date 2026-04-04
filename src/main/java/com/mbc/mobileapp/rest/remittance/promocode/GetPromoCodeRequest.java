package com.mbc.mobileapp.rest.remittance.promocode;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPromoCodeRequest extends RestRequest {
    private String promoCode;
}