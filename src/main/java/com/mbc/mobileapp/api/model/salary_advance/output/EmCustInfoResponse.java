package com.mbc.mobileapp.api.model.salary_advance.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mbc.common.api.models.ApigeeAuthenResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Response class cho eMoney digital-lending customer/info qua Apigee.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmCustInfoResponse extends ApigeeAuthenResponse {
//    private Integer status;
    private String code;
    private String message;
}
