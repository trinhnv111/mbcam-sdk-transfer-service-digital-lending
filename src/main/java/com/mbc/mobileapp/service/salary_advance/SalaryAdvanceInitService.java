package com.mbc.mobileapp.service.salary_advance;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.digital_loan.salary_advance.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

// call em -> validate -> call aml (validate)-> call msCustomer(response data)

@Service
@RequiredArgsConstructor
public class SalaryAdvanceInitService extends ChainBase {
    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
    private final DoGetCustInfoFromEM doGetCustInfoFromEM;
    private final DoValidateSalaryCust doValidateSalaryCust;
    private final DoChecAMLSalaryAdvance doChecAMLSalaryAdvance;
    private final DoGetCustInfroFromMSCust doGetCustInfroFromMSCust;
    private final DoInitSalaryAdvanceLimit doInitSalaryAdvanceLimit;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doGetCustInfoFromEM);
        addCommand(doGetCustInfroFromMSCust);
        addCommand(doValidateSalaryCust);
        addCommand(doChecAMLSalaryAdvance);
        addCommand(doInitSalaryAdvanceLimit);
    }
}
