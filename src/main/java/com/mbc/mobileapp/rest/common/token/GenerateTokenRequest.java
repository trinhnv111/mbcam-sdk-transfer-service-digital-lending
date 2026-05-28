package com.mbc.mobileapp.rest.common.token;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Data;

@Data
public class GenerateTokenRequest extends RestRequest {
    private String idCard;
    private String PhoneNumber;
}
