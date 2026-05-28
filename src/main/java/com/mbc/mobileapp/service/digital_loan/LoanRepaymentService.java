package com.mbc.mobileapp.service.digital_loan;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckDebitAccount;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.ValidateOTP;
import com.mbc.mobileapp.command.digital_loan.DoRepayment;
import com.mbc.mobileapp.command.digital_loan.DoValidateInput;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class LoanRepaymentService extends ChainBase {

    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoValidateInput doValidateInput;
    private final DoCheckDebitAccount doCheckDebitAccount;
    private final ValidateOTP validateOTP;
    private final DoRepayment doRepayment;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doValidateInput);
        addCommand(doCheckDebitAccount);
        addCommand(validateOTP);
        addCommand(doRepayment);
    }
}
