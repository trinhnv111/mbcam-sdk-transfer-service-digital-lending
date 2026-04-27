package com.mbc.mobileapp.api.model.cbc.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body cho API POST /integrate/v1/get-cbc-data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCbcDataRequest {
    private String requestBy;
    private String appCode;
    private List<String> idNumber;
}
