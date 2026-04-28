package com.mbc.mobileapp.api.model.salary_advance.output;

import com.mbc.common.api.models.ApigeeAuthenResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * Response class cho eMoney digital-lending customer/info qua Apigee.
 */
@Getter
@Setter
public class EmCustInfoResponse extends ApigeeAuthenResponse {
//    private Integer status;
    private String code;
    private String message;
}
