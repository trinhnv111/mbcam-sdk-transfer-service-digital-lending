package com.mbc.mobileapp.service.digital_loan;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.digital_loan.DoCheckDisbursementAccount;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Chain: POST /digital-loan/valid-disbursement
 *
 * 1. DoCheckRefNo              — validate refNo, chống duplicate
 * 2. CheckCustomerState        — kiểm tra trạng thái KH
 * 3. DoCheckSrvc               — kiểm tra service code
 * 4. DoCheckDisbursementAccount — toàn bộ logic:
 *      load lmt → validate limitEndDate → eMoney re-check (nếu different-day)
 *      → lấy/tạo MBC accounts → tính remaining → build ValidDisbursementResponse
 */
@Service
@RequiredArgsConstructor
public class LoanValidDisbursementService extends ChainBase {
    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
    private final DoCheckDisbursementAccount doCheckDisbursementAccount;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doCheckDisbursementAccount);
    }
}
