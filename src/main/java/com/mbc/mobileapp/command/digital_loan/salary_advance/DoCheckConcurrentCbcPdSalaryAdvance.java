package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.command.digital_loan.DoGetPd;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DoCheckConcurrentCbcPdSalaryAdvance implements Command {

    private final DoCheckCBCSalaryAdvance doCheckCBCSalaryAdvance;
    private final DoGetPd doGetPd;

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();

        try {
            log.info("[SA CREATE] Start Concurrent CBC and PD - requestId: {}", request.getRequestId());

            // Chạy đồng thời 2 Command
            CompletableFuture<Boolean> cbcFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return doCheckCBCSalaryAdvance.execute(ctx);
                } catch (Exception e) {
                    log.error("[DoCheckConcurrent] CBC error", e);
                    throw new RuntimeException(e);
                }
            });

            CompletableFuture<Boolean> pdFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return doGetPd.execute(ctx);
                } catch (Exception e) {
                    log.error("[DoCheckConcurrent] PD error", e);
                    throw new RuntimeException(e);
                }
            });

            // Chờ cả 2 hoàn thành tối đa 25 giây
            CompletableFuture.allOf(cbcFuture, pdFuture).get(25, TimeUnit.SECONDS);

            // Kiểm tra kết quả trả về từ cả 2 hàm. true nghĩa là ngắt chain (có lỗi)
            boolean cbcFailed = cbcFuture.get();
            boolean pdFailed = pdFuture.get();

            if (cbcFailed || pdFailed) {
                log.error("[DoCheckConcurrent] One of the commands failed. cbcFailed={}, pdFailed={}", cbcFailed, pdFailed);

                if (context.getResult() == null || context.getResult().isOk()) {
                    Validator.Result failResult = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
                    context.setResult(failResult);
                }
                return true; // Dừng chain
            }

        } catch (TimeoutException e) {
            // Timeout => FAIL luôn, không retry
            log.error("[DoCheckConcurrent] TIMEOUT occurred for CBC or PD - requestId: {}", request.getRequestId());
            Validator.Result result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
            context.setResult(result);
            return true;

        } catch (Exception e) {
            log.error("[DoCheckConcurrent] Exception: ", e);
            Validator.Result result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
            context.setResult(result);
            return true;
        }

        return false; // Success
    }
}
