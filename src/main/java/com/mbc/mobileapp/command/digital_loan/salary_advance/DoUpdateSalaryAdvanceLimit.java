package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DoUpdateSalaryAdvanceLimit implements Command {

    private static final String STEP_CREATE_LOAN = "CREATE_LOAN";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final ComTransDtlLmtRepository comTransDtlLmtRepo;
    private final ComTransRepo comTransRepo;
    private final ComTransProcessRepo comTransProcessRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        SalaryAdvanceCreateRequest req = request.getSalaryAdvanceCreateRequest();
        
        try {
            AppLog.info("[SA CREATE] Update limit record - requestId: " + request.getRequestId());

            Double limitAmount = (Double) context.get("sa_limit");
            String currency = (String) context.get("sa_currency");

            Optional<ComTrans> comTransOpt = comTransRepo.findById(req.getTransId());
            if (comTransOpt.isPresent()) {
                ComTrans comTrans = comTransOpt.get();
                comTrans.setStatus(Constant.STATUS_SUCCESS);
                comTransRepo.saveAndFlush(comTrans);

                ComTransProcess comTransProcess = new ComTransProcess();
                comTransProcess.setStatus(Constant.STATUS_SUCCESS);
                comTransProcess.setTransId(comTrans.getId());
                comTransProcess.setSrvcCd(comTrans.getSrvcCd());
                comTransProcessRepo.saveAndFlush(comTransProcess);
            }

            Optional<ComTransDtlLmt> tempRecordOpt = comTransDtlLmtRepo.findById(req.getTransId());
            if (tempRecordOpt.isPresent()) {
                ComTransDtlLmt tempRecord = tempRecordOpt.get();
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

                tempRecord.setMaritalStatus(req.getMaritalStatus());
                tempRecord.setAddressProvince(req.getCurrentAddressProvince());
                tempRecord.setAddressDistrict(req.getCurrentAddressDistrict());
                tempRecord.setAddressWard(req.getCurrentAddressWard());

                tempRecord.setPlaceOfBirth(req.getPlaceOfBirth());
                tempRecord.setPlaceOfBirthProvince(req.getPlaceOfBirthProvince());
                tempRecord.setPlaceOfBirthDistrict(req.getPlaceOfBirthDistrict());
                tempRecord.setPlaceOfBirthWard(req.getPlaceOfBirthWard());

                tempRecord.setEmail(req.getEmail());
                
                if (!Utility.isNull(req.getEmploymentStartDate())) {
                    try {
                        tempRecord.setEmploymentStartDate(sdf.parse(req.getEmploymentStartDate()));
                    } catch (Exception e) {}
                }

                tempRecord.setStep(STEP_CREATE_LOAN);
                tempRecord.setStatus(Constant.STATUS_SUCCESS);

                if (limitAmount != null) {
//                    tempRecord.setLimitAmount(limitAmount);
                    tempRecord.setApproveLimit(BigDecimal.valueOf(limitAmount));
                }
                tempRecord.setCurrency(currency);

                comTransDtlLmtRepo.saveAndFlush(tempRecord);
                AppLog.info("[SA CREATE] Update limit record SUCCESS - id: " + tempRecord.getId());
            } else {
                AppLog.error("[SA CREATE] transId not found in DB: " + req.getTransId());
                result = new SimpleResult("transId not found", false, ResponseCode.INVALID_INPUT.getCode());
            }

        } catch (Exception e) {
            AppLog.error("[Exception Update SA Record] requestId: " + request.getRequestId() + " desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
