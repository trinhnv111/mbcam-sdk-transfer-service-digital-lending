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
    private final DoValidateSalaryAdvanceCreate doValidateSalaryAdvanceCreate;
    private final DoUpdateSalaryAdvanceTemRecord doUpdateSalaryAdvanceTemRecord;
    private final DoGetPd doGetPd;
    private final DoValidatePDSalaryAdvance doValidatePDSalaryAdvance;
    private final DoCheckCBCSalaryAdvance doCheckCBCSalaryAdvance;
    private final GenerateOTP generateOTP;
    private final DoSendSmsMessage doSendSmsMessage;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);

        // Validate Input
        addCommand(doValidateSalaryAdvanceCreate);

        // Update DB
        addCommand(doUpdateSalaryAdvanceTemRecord);

        // Check PD
        addCommand(doGetPd);
        addCommand(doValidatePDSalaryAdvance);

        // Check CBC
        addCommand(doCheckCBCSalaryAdvance);

        // Gen OTP
        addCommand(generateOTP);

        // Send SMS
        addCommand(doSendSmsMessage);
    }
}
