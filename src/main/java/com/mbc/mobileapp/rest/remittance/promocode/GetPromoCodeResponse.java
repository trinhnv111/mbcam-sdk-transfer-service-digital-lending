package com.mbc.mobileapp.rest.remittance.promocode;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPromoCodeResponse extends BaseResponse {

    private List<String> lstPromoCode;
}
