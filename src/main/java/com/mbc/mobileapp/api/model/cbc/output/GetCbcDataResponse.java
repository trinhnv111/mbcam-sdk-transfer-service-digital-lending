package com.mbc.mobileapp.api.model.cbc.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response từ API POST /integrate/v1/get-cbc-data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCbcDataResponse {
    private Integer status;
    private String error;
    private String soaErrorCode;
    private String soaErrorDesc;
    private String clientMessageId;
    private String path;
    private List<CbcDataItem> data;
}
