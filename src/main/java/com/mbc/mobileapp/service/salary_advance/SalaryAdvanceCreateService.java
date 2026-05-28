package com.mbc.mobileapp.service.salary_advance;


import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.common.command.ValidateOTP;
import com.mbc.mobileapp.command.digital_loan.salary_advance.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class SalaryAdvanceCreateService extends ChainBase {

    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
    private final ValidateOTP validateOTP;
    private final DoValidateSalaryAdvanceCreate doValidateSalaryAdvanceCreate;
    private final DoCheckConcurrentCbcPdSalaryAdvance doCheckConcurrentCbcPdSalaryAdvance;
    //    private final DoCheckCBCSalaryAdvance doCheckCBCSalaryAdvance;
    private final DoValidatePDSalaryAdvance doValidatePDSalaryAdvance;
    private final DoOfferLimitSalaryAdvance doOfferLimitSalaryAdvance;
    private final DoUpdateSalaryAdvanceLimit doUpdateSalaryAdvanceLimit;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);

        addCommand(validateOTP);

        addCommand(doValidateSalaryAdvanceCreate);

        addCommand(doCheckConcurrentCbcPdSalaryAdvance);
        addCommand(doValidatePDSalaryAdvance);

        addCommand(doOfferLimitSalaryAdvance);

        addCommand(doUpdateSalaryAdvanceLimit);
    }
}