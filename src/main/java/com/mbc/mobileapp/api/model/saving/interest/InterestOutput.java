package com.mbc.mobileapp.api.model.saving.interest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestOutput {
        private String category;
        private String product;
        private BigDecimal taxRate;
        private List<InterestRate> interestRate;
        private String currencyAvalable;
}
