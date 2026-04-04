package com.mbc.mobileapp.api.model.transfer.ciftp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitTransOutput {
    private String minTrxAmount;
    private String maxTrxAmount;
    private String maxDayAmount;
}
