package com.mbc.mobileapp.service.saving.topup;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.saving.topup.DoValidateTopUpSaving;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class ValidateTopUpSavingService extends ChainBase {
    private final DoCheckRefNo doCheckRefNo;

    private final DoCheckSrvc doCheckSrvc;

    private final CheckCustomerState checkCustomerState;

    private final DoValidateTopUpSaving doValidateTopUpSaving;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doValidateTopUpSaving);
    }
}
