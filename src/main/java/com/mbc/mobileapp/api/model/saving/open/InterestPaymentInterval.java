package com.mbc.mobileapp.api.model.saving.open;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mbc.common.util.DateUtil;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestPaymentInterval {

    private String interestPaymentIntervalPeriod;

    @JsonFormat(pattern = DateUtil.DATE_WITH_DASH_REVERSE, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date interestPaymentDate;

}
