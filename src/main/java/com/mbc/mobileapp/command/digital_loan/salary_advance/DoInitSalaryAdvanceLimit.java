package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustomerInfo;
import com.mbc.mobileapp.api.model.salary_advance.output.EmSalaryInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class DoInitSalaryAdvanceLimit implements Command {

    private static final String LOAN_TYPE_SALARY_ADVANCE = "SALARY_ADVANCE";
    private static final String STEP_INIT = "INIT";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final ComTransDtlLmtRepository comTransDtlLmtRepo;
    private final ComTransRepo comTransRepo;
    private final ComTransProcessRepo comTransProcessRepo;
    private final ObjectMapper objectMapper;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo custInfo = context.getCustomer();

        try {
            AppLog.info("[SA INIT] Init limit record - requestId: " + request.getRequestId());

            EmCustomerInfo emCustInfo = (EmCustomerInfo) context.get("emCustomerInfo");
            EmSalaryInfo emSalaryInfo = (EmSalaryInfo) context.get("emSalaryInfo");
            CustomerInfoT24 custT24 = (CustomerInfoT24) context.get("customerInfoMS");
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

            // --- ComTrans (transaction header) ---
            ComTrans comTrans = new ComTrans();
            comTrans.setSessionId(request.getSessionId());
            comTrans.setCustId(custInfo.getId());
            comTrans.setCreatedBy(custInfo.getUserId());
            comTrans.setStatus(Constant.COM_STATUS_INT);
            comTrans.setSrvcCd(request.getSrvcCd());
            comTrans.setTransferType("INHOUSE");
            comTrans.setTransactionType("INHOUSE");
            comTransRepo.saveAndFlush(comTrans);

            // --- ComTransProcess ---
            ComTransProcess comTransProcess = new ComTransProcess();
            comTransProcess.setStatus(Constant.COM_STATUS_INT);
            comTransProcess.setTransId(comTrans.getId());
            comTransProcess.setSrvcCd(comTrans.getSrvcCd());
            comTransProcessRepo.saveAndFlush(comTransProcess);

            // --- ComTransDtlLmt ---
            ComTransDtlLmt tempRecord = new ComTransDtlLmt();
            tempRecord.setId(comTrans.getId());
            tempRecord.setTempId(comTrans.getId());
            tempRecord.setHostCifId(custInfo.getHostCifId());
            tempRecord.setNationalId(custInfo.getIdTypNo());

            // Thông tin KH
            if (custT24 != null) {
                tempRecord.setFullName(custT24.getCustomerName().getFirstName()+custT24.getCustomerName().getLastName());
                tempRecord.setGender(custT24.getGender());
                tempRecord.setNationality(custT24.getNationality());
                tempRecord.setOccupation(custT24.getOccupation());
//                tempRecord.setCompanyName(custT24.());
                if (!Utility.isNull(custT24.getDateOfBirth())) {
                    try {
                        tempRecord.setDateOfBirth(sdf.parse(emCustInfo.getDateOfBirth()));
                    } catch (Exception e) {
                        AppLog.warning("[SA INIT] Cannot parse dateOfBirth: " + emCustInfo.getDateOfBirth());
                    }
                }
            }

            tempRecord.setNationalId(custInfo.getIdTypNo());
//            tempRecord.setMaritalStatus(custT24.getMaritalStatus());

            // salaryInfo → serialize toàn bộ thành JSON lưu vào SALARY_INFO_DETAIL
            if (emSalaryInfo != null) {
                try {
                    String salaryInfoJson = objectMapper.writeValueAsString(emSalaryInfo);
                    tempRecord.setSalaryInfoDetail(salaryInfoJson);
                } catch (Exception e) {
                    AppLog.warning("[SA INIT] Cannot serialize salaryInfo: " + e.getMessage());
                }
                // Lưu riêng avg USD để tiện tính toán limit về sau
//                tempRecord.setMonthlySalaryAmountUsd(emSalaryInfo.getSalary3mAvgUSD());
//                tempRecord.setMonthlyIncome(emSalaryInfo.getSalary3mAvgUSD());
            }

            tempRecord.setLoanType(LOAN_TYPE_SALARY_ADVANCE);
            tempRecord.setStep(STEP_INIT);
            tempRecord.setStatus(Constant.COM_STATUS_INT);

            comTransDtlLmtRepo.saveAndFlush(tempRecord);

            context.put("transId", tempRecord.getId());
            AppLog.info("[SA INIT] Saved INT record - id: " + tempRecord.getId());

        } catch (Exception e) {
            AppLog.error("[Exception Save SA Init Record] requestId: " + request.getRequestId() + " desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
