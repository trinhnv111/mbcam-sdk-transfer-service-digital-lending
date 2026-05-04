package com.mbc.mobileapp.service.salary_advance;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.digital_loan.DoGetPd;
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
//    private final DoMapSalaryAdvanceOTP doMapSalaryAdvanceOTP;
//    private final ValidateOTP validateOTP;
    private final DoValidateSalaryAdvanceCreate doValidateSalaryAdvanceCreate;
    private final DoCheckCBCSalaryAdvance doCheckCBCSalaryAdvance;
    private final DoGetPd doGetPd;
    private final DoValidatePDSalaryAdvance doValidatePDSalaryAdvance;
    private final DoCalculateLimitSalaryAdvance doCalculateLimitSalaryAdvance;
    private final DoUpdateSalaryAdvanceLimit doUpdateSalaryAdvanceLimit;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);

        // Map and Verify OTP
//        addCommand(doMapSalaryAdvanceOTP);
//        addCommand(validateOTP);

        // Validate Input
        addCommand(doValidateSalaryAdvanceCreate);

        // Check CBC
        addCommand(doCheckCBCSalaryAdvance);

        // Check PD
        addCommand(doGetPd);
        addCommand(doValidatePDSalaryAdvance);

        // Calculate Limit
        addCommand(doCalculateLimitSalaryAdvance);

        // Update Record and Limit
        addCommand(doUpdateSalaryAdvanceLimit);
    }
}
