package com.mbc.mobileapp.api.model.digitalloan.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {
 *   "customer": "113054",
 *   "customerName": "...",
 *   "ldList": [...],
 *   "odList": [...]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetLoanOutput {

    private String customer;
    private String customerName;

    /** Danh sách khoản vay */
    private List<LdItem> ldList;

    /**Cụm thông tin thấu chi */
    private List<OdItem> odList;
}
