package com.mbc.mobileapp.service.user.pincode;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.ValidateOTP;
import com.mbc.mobileapp.command.user.pincode.DoCheckPinCode;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CheckPinCodeService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private ValidateOTP validateOTP;

    @Autowired
    private DoCheckPinCode doCheckPinCode;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
//        addCommand(checkCustomerState);
//        addCommand(validateOTP);
        addCommand(doCheckPinCode);
    }
}
