package com.mbc.mobileapp.service.digital_loan;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.common.command.ValidateOTP;
import com.mbc.mobileapp.command.digital_loan.DoDisbursement;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Service
@RequiredArgsConstructor
public class LoanDisbursementService extends ChainBase {
    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
//    private final ValidateOTP validateOTP;
    private final DoDisbursement doDisbursement;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
//        addCommand(validateOTP);
        addCommand(doDisbursement);
    }

}
