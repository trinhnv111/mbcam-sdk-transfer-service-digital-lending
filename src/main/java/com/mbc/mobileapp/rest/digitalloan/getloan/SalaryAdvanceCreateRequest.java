package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.bean.TokenOtp;
import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SalaryAdvanceCreateRequest extends BaseRequest {
    // hostCifId, custId,sessionId  từ BaseRequest

    /**  COM_LOAN_DISBUR_LMT trả về từ init */
    @NotNull
    private String tempRecordId;

    private TokenOtp tokenOTP;
}
