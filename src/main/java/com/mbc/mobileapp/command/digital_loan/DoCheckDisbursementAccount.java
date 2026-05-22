package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.services.il.nonsavingacct.*;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.common.validator.base.Validator.Result;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.common.api.ApiCustomer;
import com.mbc.mobileapp.api.CallMsILService;
import com.mbc.mobileapp.api.model.digitalloan.output.AccountCodeArr;
import com.mbc.mobileapp.api.model.register.NonSavingAccount;
import com.mbc.mobileapp.api.model.register.NonSavingAcctDataOutput;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoGetCustInfoFromEM;
import com.mbc.mobileapp.command.digital_loan.salary_advance.DoValidateSalaryCust;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.ValidDisbursementRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoCheckDisbursementAccount implements Command {

    private final ComTransDtlLmtRepository comTransDtlLmtRepository;
    private final DoGetCustInfoFromEM doGetCustInfoFromEM;
    private final DoValidateSalaryCust doValidateSalaryCust;
    private final CallMsILService callMsILService;
    private final ApiCustomer apiCustomer;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Result result = Result.OK;
        CustInfo custInfo = processContext.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) processContext.getResponse();
        ValidDisbursementRequest validDisbursementRequest = request.getValidDisbursementRequest();

        try {
            ComTransDtlLmt comTransDtlLmt = comTransDtlLmtRepository.findById(validDisbursementRequest.getTransId())
                    .orElseThrow(() -> new RuntimeException("ComTransDtlLmt not found with transId: " + validDisbursementRequest.getTransId()));

            LocalDate today = LocalDate.now();

            LocalDate valueDate = comTransDtlLmt.getLimitValueDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalDate endDate = comTransDtlLmt.getLimitEndDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String currency = comTransDtlLmt.getCurrency();

            // TRƯỜNG HỢP 1: Ngày yêu cầu khác ngày cấp hạn mức và chưa quá hạn
            if (!today.equals(valueDate) && !today.isAfter(endDate)) {
                log.info("[DoCheckDisbursementAccount] Ngày yêu cầu giải ngân khác ngày cấp hạn mức -> Check qua eM");

                boolean isGetCustomer = doGetCustInfoFromEM.execute(context);
                if (isGetCustomer) return true;

                boolean isValidate = doValidateSalaryCust.execute(context);
                if (isValidate) return true;
            }

            // TRƯỜNG HỢP 2: Ngày yêu cầu giải ngân BẰNG ngày cấp hạn mức
            if (today.equals(valueDate)) {
                if (custInfo != null) {
                    log.info("[DoCheckDisbursementAccount] Ngày giải ngân bằng ngày cấp hạn mức -> Gọi danh sách tài khoản IL");

                    NonSavingAcctInput inputMessage = new NonSavingAcctInput();
                    inputMessage.setCustomerId(custInfo.getHostCifId());

                    ExecuteT24Output<List<AccountBase>> iLResponse = apiCustomer.getNonSavingAccountListOtherSalary(inputMessage, custInfo.getId(), request.getRequestId());

                    // cờ kiểm tra
                    boolean hasValidAccount = false;
                    List<AccountBase> validAccountList = new ArrayList<>();

                    if (iLResponse != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(iLResponse.getStatus())) {
                        List<AccountBase> accountList = iLResponse.getData();

                        if (accountList != null && !accountList.isEmpty()) {
                            log.info("[DoCheckDisbursementAccount] Lấy danh sách tài khoản thành công, số lượng: {}", accountList.size());

                            for (AccountBase arr : accountList) {
                                // 1. Kiểm tra Currency (Phải trùng với khoản vay)
                                if (!currency.equalsIgnoreCase(arr.getAcctnCurrency())) {
                                    log.info("[DoCheckDisbursementAccount] Tài khoản {} bỏ qua vì lệch Currency: {}", arr.getAcctId(), arr.getAcctnCurrency());
                                    continue;
                                }

                                // 2. Kiểm tra Active
                                if (!Constant.ACCT_STATUS_ACTIVE.equalsIgnoreCase(arr.getAcctnStatus())) {
                                    log.info("[DoCheckDisbursementAccount] Tài khoản {} bỏ qua vì status không phải ACTIVE: {}", arr.getAcctId(), arr.getAcctnStatus());
                                    continue;
                                }

                                // 3. Kiểm tra Chặn
                                List<PostingRestrict> restrictList = arr.getPostingRestrictList();
                                if (restrictList != null && !restrictList.isEmpty()) {
                                    // Nếu mảng không rỗng, kiểm tra xem có ID chặn hợp lệ hay không
                                    boolean isBlocked = restrictList.stream()
                                            .anyMatch(r -> r != null && r.getId() != null && !r.getId().trim().isEmpty());

                                    if (isBlocked) {
                                        log.info("[DoCheckDisbursementAccount] Tài khoản {} bỏ qua vì nằm trong danh sách chặn", arr.getAcctId());
                                        continue;
                                    }
                                }

                                // 4. Kiểm tra Joint Account
//                                if (arr.getJointAccountType() != null) {
//                                    log.info("[DoCheckDisbursementAccount] Tài khoản {} bỏ qua vì là Joint Account", arr.getAcctId());
//                                    continue;
//                                }

                                // Tài khoản hợp lệ
                                validAccountList.add(arr);
                            }

                            // kết quả sau khi lọc danh sách
                            if (!validAccountList.isEmpty()) {
                                hasValidAccount = true;
                            }
                        }
                    } else {
                        log.error("[DoCheckDisbursementAccount] Lỗi lấy danh sách tài khoản từ IL: {}", iLResponse != null ? iLResponse.getErrorDesc() : "null");
                    }


                    if (hasValidAccount) {
                        log.info("[DoCheckDisbursementAccount] Thỏa mãn điều kiện (Có {} TK hợp lệ) -> Chuyển sang BƯỚC 8", validAccountList.size());
                        response.setLstNonSavingAccount(validAccountList);
                        result = Result.OK;
                    } else {
                        log.info("[DoCheckDisbursementAccount] Không có tài khoản nào thỏa mãn -> (Mở tài khoản tự động)");

                        // mở tài khoản qua MS Account
                        NonSavingAccount nonSavingAcctInput = new NonSavingAccount();
                        nonSavingAcctInput.setAccountTitle(comTransDtlLmt.getFullName());
                        nonSavingAcctInput.setBranchCode("KH0010001");
                        nonSavingAcctInput.setCategory("1001");
                        nonSavingAcctInput.setCurrency(currency);
                        nonSavingAcctInput.setCustomerId(custInfo.getHostCifId());
                        nonSavingAcctInput.setShortTitle(comTransDtlLmt.getFullName());
                        ExecuteT24Output<NonSavingAcctDataOutput> mSAccount = callMsILService.createNonSavingAccount(nonSavingAcctInput, custInfo.getId(), request.getRequestId());
                        if (mSAccount != null && Constant.CALL_MICROSERVICE_SUCCESS.equals(mSAccount.getStatus())) {
                            log.info("[DoCheckDisbursementAccount] Mở tài khoản thành công");

                            List<AccountBase> responseList = new ArrayList<>();
                            AccountBase accountBase = new AccountBase();
                            if (mSAccount.getData() != null) {
                                accountBase.setAcctId(mSAccount.getData().getAccountId());
                            }
                            accountBase.setAcctnCurrency(currency);
                            accountBase.setAcctnName(comTransDtlLmt.getFullName());

                            Balance balance = new Balance();
                            balance.setActual("0");
                            accountBase.setBalance(balance);

                            List<RelationshipManager> rmList = new ArrayList<>();
                            RelationshipManager rm = new RelationshipManager();
                            rm.setPhoneNo(custInfo.getPhoneNo());
                            rmList.add(rm);
                            accountBase.setRelationshipManager(rmList);

                            responseList.add(accountBase);
                            response.setLstNonSavingAccount(responseList);
                        } else {
                            log.error("[DoCheckDisbursementAccount] Mở tài khoản thất bại");
                            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false, ResponseCode.TRANSACTION_FAIL.getDesc());
                            processContext.setResult(result);
                            return true;
                        }

                        result = Result.OK;
                    }
                }
            }

            // TRƯỜNG HỢP 3: Quá hạn mức ngày kết thúc hạn mức
            if (today.isAfter(endDate)) {
                log.error("[Exception valid disbursement account today > limitEndDate] requestId: {}", request.getRequestId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                        ResponseCode.TRANSACTION_FAIL.getDesc());
                processContext.setResult(result);
                return true;
            }

        } catch (Exception e) {
            log.error("[Exception valid disbursement account] requestId: {} desc: {}", request.getRequestId(), JSON.stringify(e));
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false,
                    ResponseCode.TRANSACTION_FAIL.getDesc());
        }

        processContext.setResult(result);
        return !result.isOk();
    }
}