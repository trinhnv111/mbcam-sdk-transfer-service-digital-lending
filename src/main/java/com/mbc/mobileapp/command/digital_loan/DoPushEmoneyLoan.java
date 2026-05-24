package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLoanRegistration;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLoanRegistrationRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.ApiEMoney;
import com.mbc.mobileapp.api.model.digitalloan.input.EmLoanDisbursementRequest;
import com.mbc.mobileapp.api.model.salary_advance.output.EmLoanDisbursementData;
import com.mbc.mobileapp.api.model.salary_advance.output.ExcuteEmoney;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.disbursement.DisbursementRequest;
import com.mbc.mobileapp.utils.EmoneyApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Step: Ghi nợ đối tác — Gọi eMoney API loan/disbursement
 *
 * Ghi nhận khoản nợ vào hệ thống eMoney để phục vụ
 * auto-collection khi khách hàng nhận lương.
 *
 * Input từ context:
 *   - "ld_id"         → mã khoản vay T24 (từ DoCreateLoan)
 *   - "loan_currency" → loại tiền
 *   - "trans_hash"    → FT transHash nếu TH2 (CIFTP → eMoney), rỗng nếu TH1
 *
 * Output vào context:
 *   - "em_loan_id" → ID khoản nợ eMoney sinh ra (lưu để tra cứu)
 *
 * Lưu ý: Đây là bước NON-BLOCKING — nếu eMoney fail hoặc timeout,
 * cần log warning nhưng KHÔNG chặn luồng (khoản vay T24 đã tồn tại).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoPushEmoneyLoan implements Command {

    private final ApiEMoney apiEMoney;
    private final ComTransDtlLoanRegistrationRepo registrationRepo;
    private final EmoneyApiUtil emoneyApiUtil;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo custInfo = context.getCustomer();

        try {
            DisbursementRequest disbReq = request.getDisbursementRequest();

            String ldId = (String) context.get("ld_id");
            String currency = (String) context.get("loan_currency");
            String transHash = (String) context.get("ft_trans_hash"); // set bởi DoDisbursement TH2
            String registrationId = (String) context.get("registration_id");

            if (Utility.isNull(ldId)) {
                // ldId chưa có → DoCreateLoan bị skip hoặc fail → đừng chặn
                log.warn("[DoPushEmoneyLoan] ldId is null, skip eMoney push - requestId:{}", request.getRequestId());
                context.setResult(result);
                return false;
            }

            // Tìm registration để lấy limitEndDate (dueDate) và emCustomerId
            ComTransDtlLoanRegistration registration = null;
            if (!Utility.isNull(registrationId)) {
                registration = registrationRepo.findByIdAndStatus(registrationId, Constant.COM_STATUS_INT);
            }

            String dueDate = (registration != null && registration.getLoanDueDate() != null)
                    ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(registration.getLoanDueDate())
                    : LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

            String disbursementDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            // emCustomerId lưu trong ComTransDtlLmt (đã pull vào context ở DoCheckDisbursementAccount)
            String emCustomerId = (String) context.get("em_customer_id");

            // RSA encrypt: msisdn|idNumber — dùng EmoneyApiUtil (cùng key với customer/info API)
            String msisdn = custInfo.getPhoneNo();   // số điện thoại KH (format: 855XXXXXXXXX)
            String idNumber = (String) context.get("national_id"); // nationalId từ ComTransDtlLmt
            if (Utility.isNull(msisdn) || Utility.isNull(idNumber)) {
                log.warn("[DoPushEmoneyLoan] msisdn or idNumber is null, encrypt will use empty — requestId:{}", request.getRequestId());
            }
            String encrypt;
            try {
                encrypt = emoneyApiUtil.encryptCustomerInfo(
                        Utility.isNull(msisdn) ? "" : msisdn,
                        Utility.isNull(idNumber) ? "" : idNumber);
            } catch (Exception encEx) {
                log.error("[DoPushEmoneyLoan] RSA encrypt failed - requestId:{}", request.getRequestId(), encEx);
                encrypt = "";
            }

            EmLoanDisbursementRequest emReq = EmLoanDisbursementRequest.builder()
                    .MBCLoanId(ldId)
                    .encrypt(encrypt)
                    .customerId(emCustomerId)
                    .amount(disbReq.getDisburseAmount())
                    .currency(currency != null ? currency : "USD")
                    .disbursementDate(disbursementDate)
                    .dueDate(dueDate)
                    .transHash(transHash != null ? transHash : "")
                    .companyName((String) context.getOrDefault("company_name", ""))
                    .build();

            log.info("[DoPushEmoneyLoan] Calling eMoney pushLoanDisbursement - requestId:{}, ldId:{}",
                    request.getRequestId(), ldId);

            ExcuteEmoney<EmLoanDisbursementData> emOutput = apiEMoney.pushLoanDisbursement(
                    emReq, custInfo.getId(), request.getRequestId());

            if (emOutput == null) {
                // Timeout — NON-BLOCKING: ghi log warn nhưng tiếp tục
                log.warn("[DoPushEmoneyLoan] eMoney push TIMEOUT - requestId:{}, ldId:{}", request.getRequestId(), ldId);
                context.setResult(result);
                return false;
            }

            if (!Integer.valueOf(0).equals(emOutput.getStatus())) {
                // eMoney trả lỗi — NON-BLOCKING nhưng ghi log để team ops theo dõi
                log.error("[DoPushEmoneyLoan] eMoney push FAILED - status:{}, code:{}, requestId:{}",
                        emOutput.getStatus(), emOutput.getCode(), request.getRequestId());
                // Không chặn chain — khoản vay T24 đã tạo
                context.setResult(result);
                return false;
            }

            String emLoanId = emOutput.getData() != null ? emOutput.getData().getEmLoanId() : null;
            context.put("em_loan_id", emLoanId);

            // Cập nhật emLoanId vào DB nếu có field (TODO: add column EM_LOAN_ID nếu cần)
            log.info("[DoPushEmoneyLoan] SUCCESS - emLoanId:{}, requestId:{}", emLoanId, request.getRequestId());

        } catch (Exception e) {
            // NON-BLOCKING: exception không nên chặn giải ngân đã hoàn thành
            log.error("[DoPushEmoneyLoan] Exception (NON-BLOCKING) - requestId:{}", request.getRequestId(), e);
        }
        context.setResult(result);
        return false; // luôn cho chain tiếp tục
    }
}
