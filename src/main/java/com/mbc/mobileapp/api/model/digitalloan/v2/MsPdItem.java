package com.mbc.mobileapp.api.model.digitalloan.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsPdItem {

    /** Mã past due */
    private String pdId;

    /** Tổng số tiền quá hạn cần trả  */
    private String totalAmtToRepay;
}
