package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.SalaryAdvanceConstant;
import com.mbc.mobileapp.constant.MaritalStatus;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.SalaryAdvanceCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class DoUpdateSalaryAdvanceLimit implements Command {

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
                SimpleDateFormat sdf = new SimpleDateFormat(SalaryAdvanceConstant.DATE_FORMAT);

                tempRecord.setMaritalStatus(MaritalStatus.fromCode(req.getMaritalStatus()));
                tempRecord.setAddressProvince(req.getCurrentAddressProvince());
                tempRecord.setAddressDistrict(req.getCurrentAddressDistrict());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                LocalDate localDate = LocalDate.parse(req.getEmploymentStartDate(), formatter);
                Date sqlDate = java.sql.Date.valueOf(localDate);

                tempRecord.setEmploymentStartDate(sqlDate);
                tempRecord.setAddressWard(req.getCurrentAddressWard());


                tempRecord.setPlaceOfBirth(req.getPlaceOfBirth());
                tempRecord.setPlaceOfBirthProvince(req.getPlaceOfBirthProvince());
                tempRecord.setPlaceOfBirthDistrict(req.getPlaceOfBirthDistrict());
                tempRecord.setPlaceOfBirthWard(req.getPlaceOfBirthWard());

                tempRecord.setEmail(req.getEmail());

                if (req.getDisabilities() != null) {
                    tempRecord.setIsDisabilities(req.getDisabilities());
                }

//                if (!Utility.isNull(req.getEmploymentStartDate())) {
//                    try {
//                        tempRecord.setEmploymentStartDate(sdf.parse(req.getEmploymentStartDate()));
//                    } catch (Exception e) {
//                    }
//                }

                tempRecord.setStep(SalaryAdvanceConstant.STEP_CREATE_LOAN);
                tempRecord.setStatus(Constant.STATUS_SUCCESS);


                // Limit từ MS Loan
                Object saLimit = context.get("sa_limit");
                if (saLimit instanceof BigDecimal) {
                    tempRecord.setRefer_limit_amount((BigDecimal) saLimit);

                } else if (saLimit instanceof Double) {
                    tempRecord.setRefer_limit_amount((BigDecimal) saLimit);
                }

                String saLimitCurrency = (String) context.get("sa_currency");
                if (!Utility.isNull(saLimitCurrency)) {
                    tempRecord.setCurrency(saLimitCurrency);
                }



                // Ngày hiệu lực / hết hạn limit từ MS Loan
                String limitValueDate = (String) context.get("sa_limit_value_date");
                String limitEndDate = (String) context.get("sa_limit_end_date");
                if (!Utility.isNull(limitValueDate)) {
                    try {
                        tempRecord.setStartDate(sdf.parse(limitValueDate));
                    } catch (Exception ignored) {
                    }
                }
                if (!Utility.isNull(limitEndDate)) {
                    try {
                        tempRecord.setEndDate(sdf.parse(limitEndDate));
                    } catch (Exception ignored) {
                    }
                }

//                // Ngày hiệu lực / hết hạn

                context.put("sa_start_date_out", limitValueDate);
                context.put("sa_end_date_out", limitEndDate);

                Date startDate = java.sql.Date.valueOf(limitValueDate);
                Date endDate = java.sql.Date.valueOf(limitEndDate);

                tempRecord.setLimitValueDate(startDate);
                tempRecord.setLimitEndDate(endDate);
                // useLimit là nul
                BigDecimal usedLimit = Optional.ofNullable(tempRecord.getUsedLimit())
                        .orElse(BigDecimal.ZERO);

                tempRecord.setRemaining(
                        tempRecord.getRefer_limit_amount().subtract(usedLimit)
                );



                comTransDtlLmtRepo.saveAndFlush(tempRecord);
                AppLog.info("[SA CREATE] Update limit record SUCCESS - id: " + tempRecord.getId());
            } else {
                AppLog.error("[SA CREATE] transId not found in DB: " + req.getTransId());
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
            }

        } catch (Exception e) {
            AppLog.error("[Exception Update SA Record] requestId: " + request.getRequestId() + " desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
