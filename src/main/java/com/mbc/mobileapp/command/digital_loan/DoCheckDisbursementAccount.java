package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.services.il.nonsavingacct.*;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.common.api.ApiCustomer;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.CallMsILService;
import com.mbc.mobileapp.api.model.register.NonSavingAccount;
import com.mbc.mobileapp.api.model.register.NonSavingAcctDataOutput;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoGetCustInfoFromEM;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoValidateSalaryCust;
import com.mbc.mobileapp.constant.SalaryAdvanceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.ValidDisbursementRequest;
import com.mbc.mobileapp.rest.digitalloan.disbursement.ValidDisbursementResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Command duy nhất cho chain POST /digital-loan/valid-disbursement
 *
 * Thứ tự xử lý:
 *  1. Load ComTransDtlLmt theo transId
 *  2. Validate limitEndDate (hết hạn → lỗi)
 *  3a. Nếu today ≠ startDate → eMoney re-check (doGetCustInfoFromEM + doValidateSalaryCust)
 *  3b. Luôn gọi getNonSavingAccountListOtherSalary → filter → auto-create nếu rỗng
 *  4. Tính remaining = approveLimit - usedLimit, check > 0
 *  5. Build ValidDisbursementResponse:
 *     - Slider: availableAmount, minAmount, maxAmount, currency, limitEndDate
 *     - accountList: [EMONEY entry từ lmt] + [MBC accounts từ bước 3b]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoCheckDisbursementAccount implements Command {

    private final ComTransDtlLmtRepository comTransDtlLmtRepository;
    private final ApiCustomer apiCustomer;
    private final CallMsILService callMsILService;
    private final DoGetCustInfoFromEM doGetCustInfoFromEM;
    private final DoValidateSalaryCust doValidateSalaryCust;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();

        try {
            ValidDisbursementRequest validReq = request.getValidDisbursementRequest();

            // ── 1. Load hạn mức active của KH ───────────────────────────────
            // Query bằng hostCifId (session-trusted) + loanType + status=SUCCESS
            ComTransDtlLmt lmt = comTransDtlLmtRepository
                    .findTopByHostCifIdAndLoanTypeAndStatusOrderByCreatedAtDesc(
                            custInfo.getHostCifId(),
                            SalaryAdvanceConstant.LOAN_TYPE_SALARY_ADVANCE,
                            Constant.STATUS_SUCCESS);

            if (lmt == null) {
                log.error("[DoCheckDisbursementAccount] Không tìm thấy hạn mức active - hostCifId:{}", custInfo.getHostCifId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return true;
            }

            // Cross-check: transId từ FE phải khớp với hạn mức active của KH
            if (!lmt.getId().equals(validReq.getTransId())) {
                log.error("[DoCheckDisbursementAccount] transId không khớp - lmt.id:{}, req.transId:{}, hostCifId:{}",
                        lmt.getId(), validReq.getTransId(), custInfo.getHostCifId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return true;
            }

            // ── 2. Validate limitEndDate ──────────
            LocalDate today = LocalDate.now();
            LocalDate startDate = lmt.getStartDate() != null
                    ? lmt.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    : today;
            LocalDate endDate = lmt.getEndDate() != null
                    ? lmt.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    : null;

            if (endDate == null || today.isAfter(endDate)) {
                log.error("[DoCheckDisbursementAccount] Hạn mức đã hết hạn - endDate:{}, today:{}, requestId:{}",
                        endDate, today, request.getRequestId());
                result = new SimpleResult(
                        "Hạn mức ứng lương đã hết hạn. Vui lòng đăng ký lại.",
                        false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return true;
            }

            String currency = lmt.getCurrency();

            // ── 3a. eMoney re-check (chỉ khi today ≠ startDate) ────────────
            if (!today.equals(startDate)) {
                log.info("[DoCheckDisbursementAccount] Different-day → eMoney re-check, requestId:{}", request.getRequestId());
                boolean stop = doGetCustInfoFromEM.execute(context);
                if (stop) return true;
                stop = doValidateSalaryCust.execute(context);
                if (stop) return true;
            }

            // ── 3b. Lấy danh sách tài khoản MBC (luôn gọi) 
            log.info("[DoCheckDisbursementAccount] getNonSavingAccountList, requestId:{}", request.getRequestId());
            List<AccountBase> validAccountList = new ArrayList<>();

            NonSavingAcctInput inputMessage = new NonSavingAcctInput();
            inputMessage.setCustomerId(custInfo.getHostCifId());

            ExecuteT24Output<List<AccountBase>> iLResponse = apiCustomer
                    .getNonSavingAccountListOtherSalary(inputMessage, custInfo.getId(), request.getRequestId());

            if (iLResponse != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(iLResponse.getStatus())
                    && iLResponse.getData() != null) {
                for (AccountBase acct : iLResponse.getData()) {
                    if (!currency.equalsIgnoreCase(acct.getAcctnCurrency())) continue;
                    if (!Constant.ACCT_STATUS_ACTIVE.equalsIgnoreCase(acct.getAcctnStatus())) continue;
                    List<PostingRestrict> restricts = acct.getPostingRestrictList();
                    if (restricts != null && restricts.stream()
                            .anyMatch(r -> r != null && r.getId() != null && !r.getId().trim().isEmpty())) continue;
                    validAccountList.add(acct);
                }
            } else {
                log.warn("[DoCheckDisbursementAccount] Không lấy được danh sách TK từ IL: {}",
                        iLResponse != null ? iLResponse.getErrorDesc() : "null");
            }

            // Auto-create nếu không có tài khoản hợp lệ
            if (validAccountList.isEmpty()) {
                log.info("[DoCheckDisbursementAccount] Không có TK hợp lệ → auto-create, requestId:{}", request.getRequestId());
                NonSavingAccount createInput = new NonSavingAccount();
                createInput.setAccountTitle(lmt.getFullName());
                createInput.setBranchCode("KH0010001");
                createInput.setCategory("1001");
                createInput.setCurrency(currency);
                createInput.setCustomerId(custInfo.getHostCifId());
                createInput.setShortTitle(lmt.getFullName());

                ExecuteT24Output<NonSavingAcctDataOutput> created = callMsILService
                        .createNonSavingAccount(createInput, custInfo.getId(), request.getRequestId());

                if (created != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(created.getStatus())) {
                    log.info("[DoCheckDisbursementAccount] Tạo TK thành công, lấy lại danh sách TK từ IL, requestId:{}", request.getRequestId());
                    ExecuteT24Output<List<AccountBase>> iLResponseAfterCreate = apiCustomer
                            .getNonSavingAccountListOtherSalary(inputMessage, custInfo.getId(), request.getRequestId());

                    if (iLResponseAfterCreate != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(iLResponseAfterCreate.getStatus())
                            && iLResponseAfterCreate.getData() != null) {
                        for (AccountBase acct : iLResponseAfterCreate.getData()) {
                            if (!currency.equalsIgnoreCase(acct.getAcctnCurrency())) continue;
                            if (!Constant.ACCT_STATUS_ACTIVE.equalsIgnoreCase(acct.getAcctnStatus())) continue;
                            List<PostingRestrict> restricts = acct.getPostingRestrictList();
                            if (restricts != null && restricts.stream()
                                    .anyMatch(r -> r != null && r.getId() != null && !r.getId().trim().isEmpty())) continue;
                            validAccountList.add(acct);
                        }
                    } else {
                        log.error("[DoCheckDisbursementAccount] Lấy danh sách TK thất bại sau khi tạo mới, requestId:{}", request.getRequestId());
                        result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                                ResponseCode.TRANSACTION_FAIL.getCode());
                        context.setResult(result);
                        return true;
                    }
                } else {
                    log.error("[DoCheckDisbursementAccount] Auto-create TK thất bại, requestId:{}", request.getRequestId());
                    result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                            ResponseCode.TRANSACTION_FAIL.getCode());
                    context.setResult(result);
                    return true;
                }
            }

            // ── 4. Tính remaining 
            BigDecimal approveLimit = lmt.getApproveLimit() != null ? lmt.getApproveLimit() : BigDecimal.ZERO;
            BigDecimal usedLimit = lmt.getUsedLimit() != null ? lmt.getUsedLimit() : BigDecimal.ZERO;
            BigDecimal remaining = approveLimit.subtract(usedLimit);

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("[DoCheckDisbursementAccount] Đã dùng hết hạn mức - approveLimit:{}, usedLimit:{}, requestId:{}",
                        approveLimit, usedLimit, request.getRequestId());
                result = new SimpleResult("Hạn mức ứng lương đã sử dụng hết.", false,
                        ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return true;
            }

            log.info("[DoCheckDisbursementAccount] OK - remaining:{}, currency:{}, endDate:{}, requestId:{}",
                    remaining, currency, endDate, request.getRequestId());

            // ── 5. Build ValidDisbursementResponse 
            ValidDisbursementResponse resp = new ValidDisbursementResponse();
            resp.setTransId(lmt.getId());

            // Slider config
            resp.setAvailableAmount(remaining);
            resp.setMaxAmount(remaining);
            resp.setMinAmount(new BigDecimal("50"));
            resp.setCurrency(currency);
            resp.setLimitEndDate(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

            // Account list: map trực tiếp từ IL response
            // participantCode = "EMONEY" → accountType = "EMONEY" (ví eMoney)
            // participantCode null/blank  → accountType = acctnType từ T24 (OD, CURRENT...)
            List<ValidDisbursementResponse.DisbursementAccountInfo> accountInfoList = new ArrayList<>();
            boolean hasEmoneyFromIL = false;

            for (AccountBase acct : validAccountList) {
                ValidDisbursementResponse.DisbursementAccountInfo info = new ValidDisbursementResponse.DisbursementAccountInfo();
                info.setAcctId(acct.getAcctId());
                info.setAcctnCurrency(acct.getAcctnCurrency());
                info.setAcctnName(acct.getAcctnName());
                if (acct.getBalance() != null) info.setActual(acct.getBalance().getActual());
                if (acct.getRelationshipManager() != null && !acct.getRelationshipManager().isEmpty()) {
                    info.setPhoneNo(acct.getRelationshipManager().get(0).getPhoneNo());
                }
                info.setParticipantCode(acct.getParticipantCode());

                // Phân loại theo participantCode
                if ("EMONEY".equalsIgnoreCase(acct.getParticipantCode())) {
                    info.setAccountType("EMONEY");
                    hasEmoneyFromIL = true;
                } else {
                    info.setAccountType(acct.getAcctnType());
                }
                accountInfoList.add(info);
            }

            // Fallback: nếu IL không trả eMoney account → build từ lmt (đã lưu lúc init-hard code)
            if (!hasEmoneyFromIL && !Utility.isNull(lmt.getEmCustomerId())) {
                ValidDisbursementResponse.DisbursementAccountInfo emoneyAcct = new ValidDisbursementResponse.DisbursementAccountInfo();
                emoneyAcct.setAcctId(lmt.getEmCustomerId());
                emoneyAcct.setAcctnName(lmt.getFullName());
                emoneyAcct.setAcctnCurrency(currency);
                emoneyAcct.setPhoneNo(custInfo.getPhoneNo());
                emoneyAcct.setAccountType("EMONEY");
                emoneyAcct.setActual("0");
                accountInfoList.add(0, emoneyAcct); // đặt lên đầu danh sách
            }


            resp.setAccountList(accountInfoList);
            response.setValidDisbursementResponse(resp);

        } catch (Exception e) {
            log.error("[DoCheckDisbursementAccount] Exception - requestId:{}, desc: {}", request.getRequestId(), e.toString());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        context.setResponse(response);
        return !result.isOk();
    }
}