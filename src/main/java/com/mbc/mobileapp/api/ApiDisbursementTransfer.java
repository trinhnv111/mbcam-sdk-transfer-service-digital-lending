package com.mbc.mobileapp.api;

import com.mbc.common.api.ApiFundsTransfer;
import com.mbc.common.api.models.fundsTransfer.*;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlLoanDisbursement;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.constant.SalaryAdvanceConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ApiDisbursementTransfer — Wrapper API gọi MS FT (Funds Transfer) cho luồng giải ngân Salary Advance.
 * 
 * <p>Định vị trong package com.mbc.mobileapp.api theo đúng convention gọi API của hệ thống.
 * Đóng gói toàn bộ logic khởi tạo DTO FundsTransferInfo, xử lý retry chặng 3, và bắt lỗi Idempotent 409/404.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiDisbursementTransfer {

    private static final String SOA_TIMEOUT = "002";

    private final ApiFundsTransfer apiFundsTransfer;

    // ═══════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Chặng 2 — INHOUSE: drawdownAccount → Current Account KH.
     */
    public ExecuteT24Output<DataOutput> makeInhouseTransfer(
            ComTrans comTrans,
            ComTransDtlLoanDisbursement dtl,
            CustInfo custInfo,
            String remark,
            String requestId) throws Exception {

        FundsTransferInfo info = new FundsTransferInfo();

        DebitAccount da = new DebitAccount();
        da.setAccountNumber(dtl.getDebitAcctNo());
        da.setAccountName(dtl.getDebitAcctName());
        da.setAccountCurrency(dtl.getDebitAcctCcy());
        da.setAccountType("ACCOUNT");
        info.setDebitAccount(da);

        CreditAccount ca = new CreditAccount();
        ca.setAccountNumber(dtl.getCrebitAcctNo());
        ca.setAccountName(dtl.getCrebitAcctName());
        ca.setAccountCurrency(dtl.getCrebitAcctCcy());
        ca.setAccountType("ACCOUNT");
        info.setCreditAccount(ca);

        Amount amount = new Amount();
        amount.setAmount(dtl.getDebitAmount());
        amount.setCurrency(dtl.getDebitAcctCcy());
        info.setAmount(amount);

        info.setCreditBank(new CreditBank());
        info.setBranchCode(custInfo.getSrvcPcCd());
        info.setRemark(remark);
        info.setTransferType(SalaryAdvanceConstant.TRANSFER_TYPE_INHOUSE);

        List<AddInfoList> addInfo = new ArrayList<>();
        AddInfoList ob = new AddInfoList();
        ob.setName("ORDERING_BANK");
        ob.setValue("MBC");
        addInfo.add(ob);
        info.setAddInfoList(addInfo);

        log.info("[ApiDisbursementTransfer] INHOUSE transfer - requestId:{}", requestId);
        return apiFundsTransfer.makeTransfer(info, custInfo.getId(), requestId, comTrans.getId());
    }

    /**
     * Chặng 3 — CIFTP: Current Account KH → Ví eMoney, với retry.
     */
    public ExecuteT24Output<DataOutput> makeCiftpTransferWithRetry(
            ComTrans comTrans,
            String debitAcctNo, String debitAcctName, String debitAcctCcy,
            String walletNo, String walletName, String walletCcy,
            String amountStr,
            CustInfo custInfo,
            String remark,
            String requestId) throws Exception {

        FundsTransferInfo info = new FundsTransferInfo();

        DebitAccount da = new DebitAccount();
        da.setAccountNumber(debitAcctNo);
        da.setAccountName(debitAcctName);
        da.setAccountCurrency(debitAcctCcy);
        da.setAccountType("ACCOUNT");
        info.setDebitAccount(da);

        CreditAccount ca = new CreditAccount();
        ca.setAccountNumber(walletNo);
        ca.setAccountName(walletName);
        ca.setAccountCurrency(walletCcy);
        ca.setAccountType("ACCOUNT");
        info.setCreditAccount(ca);

        BigDecimal amountBD;
        try {
            amountBD = new BigDecimal(amountStr);
        } catch (Exception e) {
            amountBD = BigDecimal.ZERO;
        }

        Amount amount = new Amount();
        amount.setAmount(amountBD);
        amount.setCurrency(debitAcctCcy);
        amount.setBenAmount(amountBD);
        amount.setBenCurrency(walletCcy);
        info.setAmount(amount);

        info.setCreditBank(new CreditBank());
        info.setBranchCode(custInfo.getSrvcPcCd());
        info.setRemark(remark);
        info.setTransferType(SalaryAdvanceConstant.TRANSFER_TYPE_CIFTP);

        List<AddInfoList> addInfo = new ArrayList<>();
        AddInfoList ob = new AddInfoList();
        ob.setName("ORDERING_BANK");
        ob.setValue("MBC");
        addInfo.add(ob);

        AddInfoList orgData = new AddInfoList();
        orgData.setName("ORG.DATA");
        orgData.setValue("SENDER");
        addInfo.add(orgData);
        info.setAddInfoList(addInfo);

        int maxRetry = SalaryAdvanceConstant.CIFTP_MAX_RETRY;
        ExecuteT24Output<DataOutput> output = null;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            log.info("[ApiDisbursementTransfer] CIFTP attempt {}/{} - requestId:{}", attempt, maxRetry, requestId);
            output = apiFundsTransfer.makeTransfer(info, custInfo.getId(), requestId, comTrans.getId());

            if (output == null) {
                log.warn("[ApiDisbursementTransfer] CIFTP attempt {} null response - requestId:{}", attempt, requestId);
                continue;
            }
            if (isTransferSuccess(output)) {
                log.info("[ApiDisbursementTransfer] CIFTP OK at attempt {} - requestId:{}", attempt, requestId);
                return output;
            }
            if (SOA_TIMEOUT.equals(output.getSoaErrorCode())) {
                log.warn("[ApiDisbursementTransfer] CIFTP attempt {} soaErrorCode=002 - requestId:{}", attempt, requestId);
                continue;
            }
            // Lỗi nghiệp vụ → dừng
            log.error("[ApiDisbursementTransfer] CIFTP attempt {} error - status:{}, soaCode:{}, requestId:{}",
                    attempt, output.getStatus(), output.getSoaErrorCode(), requestId);
            return output;
        }

        log.error("[ApiDisbursementTransfer] CIFTP exhausted {} retries - requestId:{}", maxRetry, requestId);
        return output;
    }

    /**
     * Kiểm tra CIFTP output có phải thành công không.
     * Thành công = 200 OK hoặc 409/404 (duplicate = đã hạch toán).
     */
    public boolean isTransferSuccess(ExecuteT24Output<DataOutput> output) {
        if (output == null) return false;
        if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) return true;
        return "409".equals(output.getStatus()) && "404".equals(output.getSoaErrorCode());
    }
}
