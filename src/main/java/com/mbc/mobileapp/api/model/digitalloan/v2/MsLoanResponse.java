package com.mbc.mobileapp.api.model.digitalloan.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mbc.common.api.models.ApiAuthenResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsLoanResponse<T> extends ApiAuthenResponse {

    private Integer status;
    private String path;
    private String clientMessageId;
    private T data;
}
