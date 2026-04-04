package com.mbc.mobileapp.api.model.saving.cob;

import lombok.Data;

@Data
public class CheckCoBInput {
    private String action;
    private String destination;
    private String version;
    private String branchCode;
}
