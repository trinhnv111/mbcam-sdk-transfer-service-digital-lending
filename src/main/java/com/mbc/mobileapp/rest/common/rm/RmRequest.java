package com.mbc.mobileapp.rest.common.rm;

import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RmRequest extends RestRequest {

    private String rmCode;
    private String rmMobile;
}
