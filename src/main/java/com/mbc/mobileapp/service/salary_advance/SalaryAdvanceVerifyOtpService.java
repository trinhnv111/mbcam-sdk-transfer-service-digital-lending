package com.mbc.mobileapp.service.salary_advance;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.digital_loan.salary_advance.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class SalaryAdvanceVerifyOtpService extends ChainBase {

    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
    private final ValidateOTP validateOTP;
    private final DoCalculateLimitSalaryAdvance doCalculateLimitSalaryAdvance;
    private final DoUpdateSalaryAdvanceStatus doUpdateSalaryAdvanceStatus;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);

        // Validate OTP
        addCommand(validateOTP);

        // Calculate Limit (Mock)
        addCommand(doCalculateLimitSalaryAdvance);

        // Update DB Status
        addCommand(doUpdateSalaryAdvanceStatus);
    }
}
