package com.mbc.mobileapp.api.model.saving.interest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestInput {

    private String resident;

    private String productCode;
}
