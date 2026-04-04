package com.mbc.mobileapp.api.model.saving.open;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mbc.common.util.DateUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OpenSavingOutput {
        private String interestRate;
        private String locTerm;
        @JsonFormat(pattern = DateUtil.DATE_WITH_SLASH, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
        private Date maturityDate;
        @JsonFormat(pattern = DateUtil.DATE_WITH_SLASH, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
        private Date valueDate;
        private String nominateAccount;
        private BigDecimal principalAmt;
        private String t24VersionId;
        private String maturityInstruction;
        private String nameAccountDeposit;
}
