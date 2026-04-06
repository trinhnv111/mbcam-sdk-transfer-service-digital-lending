package com.mbc.mobileapp.command.salaryadvance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlSaCreditLimit;
import com.mbc.common.entity.ComTransDtlSaLoan;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlSaCreditLimitRepo;
import com.mbc.common.repository.ComTransDtlSaLoanRepo;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.SaResponseCode;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoSaGetCreditLimit implements Command {

    private final ComTransDtlSaCreditLimitRepo creditLimitRepo;
    private final ComTransDtlSaLoanRepo saLoanRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        // Lấy request từ context
        com.mbc.mobileapp.rest.bean.CommonServiceRequest request =
                (com.mbc.mobileapp.rest.bean.CommonServiceRequest) context.getRequest();
        CommonServiceResponse response =
                (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();

        try {
            String custId = custInfo.getId();

            // 1. Query hạn mức ACTIVE của KH
            ComTransDtlSaCreditLimit creditLimit =
                    creditLimitRepo.findByCustIdAndLimitStatus(custId, "ACTIVE");

            if (creditLimit == null) {
                // Không có hạn mức – trả về lỗi nghiệp vụ
                result = new SimpleResult(
                        SaResponseCode.SA_NO_CREDIT_LIMIT.getErrorDesc(), false,
                        SaResponseCode.SA_NO_CREDIT_LIMIT.getErrorCode());
                context.setResult(result);
                return !result.isOk();  // return true = dừng chain
            }

            // 2. Kiểm tra có khoản vay ACTIVE/OVERDUE không
            List<ComTransDtlSaLoan> activeLoans = saLoanRepo
                    .findByCustIdAndLoanStatusIn(custId, List.of("ACTIVE", "OVERDUE"));

            boolean hasActiveLoan = !activeLoans.isEmpty();
            String activeLoanCode = hasActiveLoan ? activeLoans.get(0).getLoanCode() : null;

            // 3. Gắn dữ liệu vào response để ServiceImpl đọc
            response.setSaCreditLimit(creditLimit);
            // Dùng putVar để truyền thêm data phụ không có field sẵn trong response
            context.putVar("SA_HAS_ACTIVE_LOAN", hasActiveLoan);
            context.putVar("SA_ACTIVE_LOAN_CODE", activeLoanCode);

        } catch (Exception e) {
            log.error("[DoSaGetCreditLimit] requestId: {}, error: {}",
                    request.getRequestId(), e.getMessage(), e);
            result = new SimpleResult(
                    ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        context.setResponse(response);
        return !result.isOk();
    }
}