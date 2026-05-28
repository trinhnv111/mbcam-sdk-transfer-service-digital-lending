package com.mbc.mobileapp.rest.bean;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RestRequest extends BaseRequest {

    @NotNull
    @NotBlank
    private String partner;

    private String tid;

}
