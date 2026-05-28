package com.mbc.mobileapp.service.remittance;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.remittance.DoRemittanceBankList;
import com.mbc.mobileapp.command.remittance.DoRemittanceMakeTransferInit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class ValidateTransferInitService extends ChainBase {


    private final DoCheckRefNo doCheckRefNo;

    private final CheckCustomerState checkCustomerState;

    private final DoCheckSrvc doCheckSrvc;

    private final DoCheckDebitAccount doCheckDebitAccount;
    
//    private final DoRemittanceGetAccountName doRemittanceGetAccountName;
     
    private final DoRemittanceBankList doRemittanceBankList;

    private final DoRemittanceMakeTransferInit doRemittanceMakeTransferInit;
    
    private final DoGetLimitEasyPaymentUsed doGetLimitEasyPaymentUsed;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doCheckDebitAccount);
        addCommand(doRemittanceBankList);
        addCommand(doRemittanceMakeTransferInit);
        addCommand(doGetLimitEasyPaymentUsed);
    }
}
