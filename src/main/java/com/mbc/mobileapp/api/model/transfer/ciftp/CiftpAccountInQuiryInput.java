package com.mbc.mobileapp.api.model.transfer.ciftp;

import com.mbc.mobileapp.constant.CommonServiceConstant.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CiftpAccountInQuiryInput extends AccountInQuiryInput {

    private Service service;
    private String subType;

}
