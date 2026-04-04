package com.mbc.mobileapp.service.saving.close;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.saving.DoCheckCob;
import com.mbc.mobileapp.command.saving.close.DoValidateDepositClosure;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class ValidateDepositClosureService extends ChainBase {
    private final DoCheckRefNo doCheckRefNo;

    private final DoCheckSrvc doCheckSrvc;

    private final CheckCustomerState checkCustomerState;

    private final DoCheckCob doCheckCob;

    private final DoValidateDepositClosure doValidateDepositClosure;

    @PostConstruct
    public void addCommand(){
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doCheckCob);
        addCommand(doValidateDepositClosure);
    }
}
