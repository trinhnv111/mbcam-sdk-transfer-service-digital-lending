package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceVerifyOtpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoUpdateSalaryAdvanceStatus implements Command {

    private final ComTransDtlLmtRepository comTransDtlLmtRepo;
    private final ComTransProcessRepo comTransProcessRepo;

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        SalaryAdvanceVerifyOtpRequest verifyRequest = request.getSalaryAdvanceVerifyOtpRequest();

        try {
            Optional<ComTransDtlLmt> optRecord = comTransDtlLmtRepo.findById(verifyRequest.getTempRecordId());
            if (!optRecord.isPresent()) {
                log.error("[DoUpdateSalaryAdvanceStatus] tempRecordId {} not found", verifyRequest.getTempRecordId());
                result = new SimpleResult("Record not found", false, ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            ComTransDtlLmt record = optRecord.get();
            
            Double limit = (Double) context.get("sa_limit");
            if (limit != null) {
                record.setApproveLimit(BigDecimal.valueOf(limit));
                comTransDtlLmtRepo.saveAndFlush(record);
            }

            ComTransProcess process = comTransProcessRepo.findByTransId(record.getId());
            if (process != null) {
                process.setStatus(Constant.STATUS_SUCCESS);
                comTransProcessRepo.saveAndFlush(process);
            }

        } catch (Exception e) {
            log.error("[DoUpdateSalaryAdvanceStatus] Exception: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }
}
