package com.mbc.mobileapp.service.transfer.khqr;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.transfer.khqr.DoKHQRCheckInfo;
import com.mbc.mobileapp.command.transfer.khqr.DoKHQRExecuteTransfer;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class KHQRExecuteTransfer extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private DoKHQRExecuteTransfer doKHQRExecuteTransfer;


    @PostConstruct
    public void addCommandChain() {
//        addCommand(doCheckRefNo);
//        addCommand(checkCustomerState);
        addCommand(doKHQRExecuteTransfer);

    }
}
