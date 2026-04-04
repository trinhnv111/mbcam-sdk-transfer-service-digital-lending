package com.mbc.mobileapp.service.saving;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.saving.DoCheckCob;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class CheckCoBService extends ChainBase {

    private final DoCheckRefNo doCheckRefNo;

    private final DoCheckSrvc doCheckSrvc;

    private final CheckCustomerState checkCustomerState;

    private final DoCheckCob doCheckCob;

    @PostConstruct
    public void addCommand(){
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doCheckCob);
    }
}
