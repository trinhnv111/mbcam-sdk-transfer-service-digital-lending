package com.mbc.mobileapp.api.model.transfer.ciftp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CiftpAccountInquiryOutput extends AccountInquiryOutput {
    private Boolean frozen;
    private String kycStatus;
    private String accountStatus;
    private String bakongAccountID;
    private String bankParticipantCode;
    private String bankName;
    private String phone;
}
