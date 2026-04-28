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
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoUpdateSalaryAdvanceTemRecord implements Command {

    private final ComTransDtlLmtRepository comTransDtlLmtRepo;
    private final ComTransProcessRepo comTransProcessRepo;

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        SalaryAdvanceCreateRequest createRequest = request.getSalaryAdvanceCreateRequest();

        try {
            Optional<ComTransDtlLmt> optRecord = comTransDtlLmtRepo.findById(createRequest.getTempRecordId());
            if (!optRecord.isPresent()) {
                log.error("[DoUpdateSalaryAdvanceTemRecord] tempRecordId {} not found", createRequest.getTempRecordId());
                result = new SimpleResult("Record not found", false, ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            ComTransDtlLmt record = optRecord.get();
            record.setEmail(createRequest.getEmail());
            
            if (!Utility.isNull(createRequest.getEmploymentStartDate())) {
                try {
                    // Assuming format is dd/MM/yyyy based on standard or yyyyMMdd
                    // Need to check the frontend format, usually it's yyyyMMdd or yyyy-MM-dd
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    Date startDate = sdf.parse(createRequest.getEmploymentStartDate().replaceAll("-", "").replaceAll("/", ""));
                    record.setEmploymentStartDate(startDate);
                } catch (Exception ex) {
                    log.error("Error parsing employmentStartDate: ", ex);
                }
            }
            
            record.setMaritalStatus(createRequest.getMaritalStatus());
            record.setAddressProvince(createRequest.getCurrentAddressProvince());
            record.setAddressDistrict(createRequest.getCurrentAddressDistrict());
            record.setAddressWard(createRequest.getCurrentAddressWard());

            comTransDtlLmtRepo.saveAndFlush(record);

            // Update ComTransProcess to 'PRO' (Processing)
            ComTransProcess process = new ComTransProcess();
            process.setTransId(record.getId()); // the parent ComTrans ID is same as ComTransDtlLmt ID
            process.setSrvcCd(Constant.SrvcCd.SRVC_SALARY_ADVANCE);
            process.setStatus(Constant.COM_STATUS_PRO);
//            process.setCreatedBy(context.getCustomer().getUserId());
            comTransProcessRepo.saveAndFlush(process);

            context.put("comTransDtlLmt", record);
            
            // To be used by GenerateOTP
            context.put("transId", record.getId());

        } catch (Exception e) {
            log.error("[DoUpdateSalaryAdvanceTemRecord] Exception: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }
}
