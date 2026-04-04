package com.mbc.mobileapp.service.user.pincode;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.ValidateOTP;
import com.mbc.mobileapp.command.user.pincode.DoCheckCustomerInfo;
import com.mbc.mobileapp.command.user.pincode.DoResetPinCode;
import com.mbc.mobileapp.command.user.pincode.DoSetPinCode;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ResetPinCodeService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoCheckCustomerInfo doCheckCustomerInfo;

    @Autowired
    private ValidateOTP validateOTP;

    @Autowired
    private DoResetPinCode doResetPinCode;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doCheckCustomerInfo);
        addCommand(validateOTP);
        addCommand(doResetPinCode);
    }
}
