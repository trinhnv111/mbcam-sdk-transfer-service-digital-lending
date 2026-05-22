package com.mbc.mobileapp.service.salary_advance;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoGetSalaryAdvanceOfferLimitService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor

public class GetSalaryAdvanceOfferLimitService extends ChainBase {
    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
    private final DoGetSalaryAdvanceOfferLimitService doGetSalaryAdvanceOfferLimitService;

    @PostConstruct
    public void addCommandChain(){
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doGetSalaryAdvanceOfferLimitService);
    }
}
