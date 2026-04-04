package com.mbc.mobileapp.service.saving;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.saving.DoCalculateTotalAmountDeposit;
import com.mbc.mobileapp.command.saving.DoGetInterest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class GetInterestService extends ChainBase {

    private final DoCheckRefNo doCheckRefNo;

    private final DoCheckSrvc doCheckSrvc;

    private final CheckCustomerState checkCustomerState;

    private final DoGetInterest doGetInterest;

    private final DoCalculateTotalAmountDeposit doCalculateTotalAmountDeposit;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doGetInterest);
        addCommand(doCalculateTotalAmountDeposit);
    }


}
