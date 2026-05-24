package com.mbc.mobileapp.api.model.loanorigination.output;

import lombok.Getter;
import lombok.Setter;

/**
 * Output của DoGenFileDisbursement — chứa nội dung PDF base64 cho 2 ngôn ngữ
 */
@Getter
@Setter
public class DoGenFileOutput {
    /** transId của lần giải ngân (=ComTransDtlLmt.id) */
    private String transId;

    /** Nội dung file PDF hợp đồng tiếng Anh (base64) */
    private String fileContentEng;

    /** Nội dung file PDF hợp đồng tiếng Khmer (base64) */
    private String fileContentKhr;
}
