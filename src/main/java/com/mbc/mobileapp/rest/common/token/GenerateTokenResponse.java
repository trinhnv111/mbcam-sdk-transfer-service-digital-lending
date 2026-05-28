package com.mbc.mobileapp.rest.common.token;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Data;

@Data
public class GenerateTokenResponse extends BaseResponse {
    private String token;
}
