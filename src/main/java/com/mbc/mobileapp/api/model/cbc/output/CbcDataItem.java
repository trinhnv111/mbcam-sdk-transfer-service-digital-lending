package com.mbc.mobileapp.api.model.cbc.output;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Một item trong data[] của GetCbcDataResponse.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CbcDataItem {
    private String statusCode;
    private String statusDesc;
    private String idNumber;
    private Long reportId;
    private String currentStatus;
    private String history12MStatus;
    private String history24MStatus;
    @SuppressWarnings("java:S116")
    private String ENQUIRY_REFERENCE;
    /** cbcData: dành cho appCode != BPM (MOBILEAPP / CAMID) */
    private JsonNode cbcData;
    /** cbcResult: dành cho appCode = BPM */
    private JsonNode cbcResult;
}
