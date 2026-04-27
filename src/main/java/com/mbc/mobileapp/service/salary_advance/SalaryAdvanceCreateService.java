package com.mbc.mobileapp.service.salary_advance;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.common.command.ValidateOTP;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoCallMsLoanCalcLimit;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoCheckCbc;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoCheckPd;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoSaveSalaryAdvanceRecord;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Command chain cho API POST /salary-advance/create

 */
@Service
@RequiredArgsConstructor
public class SalaryAdvanceCreateService extends ChainBase {

    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
    private final DoCheckPd doCheckPd;
    private final DoCheckCbc doCheckCbc;
    private final ValidateOTP validateOTP;
    private final DoCallMsLoanCalcLimit doCallMsLoanCalcLimit;
    private final DoSaveSalaryAdvanceRecord doSaveSalaryAdvanceRecord;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doCheckPd);
        addCommand(doCheckCbc);
        addCommand(validateOTP);
        addCommand(doCallMsLoanCalcLimit);
        addCommand(doSaveSalaryAdvanceRecord);
    }
}
