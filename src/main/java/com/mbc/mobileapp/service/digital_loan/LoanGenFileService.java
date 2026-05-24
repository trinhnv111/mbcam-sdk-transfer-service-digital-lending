package com.mbc.mobileapp.service.digital_loan;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.DoCheckSrvc;
import com.mbc.mobileapp.command.digital_loan.DoGenFileDisbursement;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Chain: POST /digital-loan/genfile
 *
 * 1. DoCheckRefNo          — validate refNo chống duplicate
 * 2. CheckCustomerState    — kiểm tra trạng thái KH
 * 3. DoCheckSrvc           — kiểm tra service code
 * 4. DoGenFileDisbursement — gen ENG+KHR contract PDF via BIRT → upload FTS → save docIds
 */
@Service
@RequiredArgsConstructor
public class LoanGenFileService extends ChainBase {

    private final DoCheckRefNo doCheckRefNo;
    private final CheckCustomerState checkCustomerState;
    private final DoCheckSrvc doCheckSrvc;
    private final DoGenFileDisbursement doGenFileDisbursement;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doGenFileDisbursement);
    }
}
