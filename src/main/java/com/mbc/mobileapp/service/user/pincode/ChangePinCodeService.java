package com.mbc.mobileapp.service.user.pincode;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.ValidateOTP;
import com.mbc.mobileapp.command.user.pincode.DoChangePinCode;
import com.mbc.mobileapp.command.user.pincode.DoSetPinCode;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ChangePinCodeService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private ValidateOTP validateOTP;

    @Autowired
    private DoChangePinCode doChangePinCode;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(validateOTP);
        addCommand(doChangePinCode);
    }
}
