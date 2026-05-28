package com.mbc.mobileapp.service.digital_loan;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.digital_loan.DoCheckDisbursementAccount;
import com.mbc.mobileapp.command.digital_loan.DoGetLoanInfo;
import com.mbc.mobileapp.command.digital_loan.DoValidDisbursement;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class LoanValidDisbursementService extends ChainBase {
    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
//    private final DoGetLoanInfo doGetLoanInfo;
    private final DoCheckDisbursementAccount doCheckDisbursementAccount;
//    private final DoValidDisbursement doValidDisbursement;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
//        addCommand(doGetLoanInfo);
        addCommand(doCheckDisbursementAccount);
//        addCommand(doValidDisbursement);
    }
}
