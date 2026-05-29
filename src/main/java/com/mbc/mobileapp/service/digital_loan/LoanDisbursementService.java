package com.mbc.mobileapp.service.digital_loan;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.common.command.ValidateOTP;
import com.mbc.mobileapp.command.digital_loan.DoCreateLoan;
import com.mbc.mobileapp.command.digital_loan.DoDisbursement;
import com.mbc.mobileapp.command.digital_loan.DoPushEmoneyLoan;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Chain: POST /loan/disbursement (payment step)
 *
 * 1. DoCheckRefNo        — validate refNo, chống duplicate
 * 2. CheckCustomerState  — kiểm tra trạng thái KH (active, not blocked)
 * 3. DoCheckSrvc         — kiểm tra service code hợp lệ
 * 4. ValidateOTP         — xác thực OTP trước khi giải ngân
 * 5. DoCreateLoan        — hạch toán Core: gọi MS Loan → T24 mở LD account → nhận ldId
 * 6. DoDisbursement      — Make Transfer: đẩy tiền về TK đích (TH1: INHOUSE | TH2: CIFTP+retry)
 * 7. DoPushEmoneyLoan    — Ghi nợ eMoney (NON-BLOCKING: không chặn chain nếu fail)
 */
@Service
@RequiredArgsConstructor
public class LoanDisbursementService extends ChainBase {

    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
    private final ValidateOTP validateOTP;
    private final DoCreateLoan doCreateLoan;
    private final DoDisbursement doDisbursement;
    private final DoPushEmoneyLoan doPushEmoneyLoan;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(validateOTP);
        addCommand(doCreateLoan);
        addCommand(doDisbursement);
        addCommand(doPushEmoneyLoan);
    }
}

