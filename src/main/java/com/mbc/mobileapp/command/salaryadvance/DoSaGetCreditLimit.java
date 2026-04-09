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
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        com.mbc.mobileapp.rest.bean.CommonServiceRequest request = (com.mbc.mobileapp.rest.bean.CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();

        try {
            String custId = custInfo.getId();

            // 1. Query hạn mức ACTIVE của KH
            ComTransDtlSaCreditLimit creditLimit = creditLimitRepo.findByCustIdAndLimitStatus(custId, "ACTIVE");

            //hông có limit => OK + hasLimit=false (KHÔNG fail) ->  "result": { "ok": true, "code": "000", "message": "OK" },
            if (creditLimit == null) {
                // Không có hạn mức – trả về lỗi nghiệp vụ
//                result = new SimpleResult(
//                        SaResponseCode.SA_NO_CREDIT_LIMIT.getErrorDesc(), false,
//                        SaResponseCode.SA_NO_CREDIT_LIMIT.getErrorCode());

                context.putVar("SA_HAS_LIMIT", false);
                context.putVar("SA_LOAN_OBJ", null);
                response.setSaCreditLimit(null);
                context.setResult(result);
                return false;  // k dừng chain
            }

            // 2. Kiểm tra có khoản vay ACTIVE/OVERDUE không
            context.putVar("SA_HAS_LIMIT", true);// có hạn mức

            ComTransDtlSaLoan activeLoans = saLoanRepo.
                    findTopByCustIdAndLoanStatusInOrderByDisbursementDateDesc(custId, List.of("ACTIVE", "OVERDUE")).orElse(null);

//            ComTransDtlSaLoan  activeLoan = activeLoans.isEmpty() ? null : activeLoans.get(0) ;
            context.putVar("SA_LOAN_OBJ", activeLoans);

            // 3. Gắn dữ liệu vào response để ServiceImpl đọc
            response.setSaCreditLimit(creditLimit);


        } catch (Exception e) {
            log.error("[DoSaGetCreditLimit] requestId: {}, error: {}", request.getRequestId(), e.getMessage(), e);
            result = new SimpleResult(
                    ResponseCode.TRANSACTION_FAIL.getDesc(),
                    false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        context.setResponse(response);
        return !result.isOk();
    }
}