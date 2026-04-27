package com.mbc.mobileapp.command.digital_loan.salary_advance;

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

/**
 * Command: Lưu bản ghi tạm vào COM_LOAN_DISBUR_LMT
 * Theo pattern chuẩn:
 *   1. Tạo ComTrans (bảng cha) → Hibernate auto-gen UUID
 *   2. Dùng comTrans.getId() cho bảng chi tiết ComTransDtlLmt
 *   3. Tạo ComTransProcess để track giao dịch
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoSavaSalaryAdvanceTemRecord implements Command {

    private static final String LOAN_TYPE_SALARY_ADVANCE = "SALARY_ADVANCE";
    private static final String STEP_CHECK_CUST = "CHECK_CUST";
    private static final String STATUS_SUCCESS = "Success";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final ComTransRepo comTransRepo;
    private final ComTransProcessRepo comTransProcessRepo;
    private final ComTransDtlLmtRepository comTransDtlLmtRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo custInfo = context.getCustomer();

        try {
            AppLog.info("[SA INIT - SAVE TEMP RECORD] Start - requestId: " + request.getRequestId());

            EmCustomerInfo emCustInfo = (EmCustomerInfo) context.get("emCustomerInfo");
            EmSalaryInfo emSalaryInfo = (EmSalaryInfo) context.get("emSalaryInfo");
            CustomerInfoT24 custT24 = (CustomerInfoT24) context.get("customerInfoMS");
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

            //  1. Tạo ComTrans UUID 
            ComTrans comTrans = new ComTrans();
            comTrans.setCustId(custInfo.getId());
            comTrans.setSrvcCd(request.getSrvcCd());
            comTrans.setCreatedBy(custInfo.getUserId());
            comTrans.setStatus(Constant.COM_STATUS_INT);
            comTrans.setSessionId(request.getSessionId());
            comTrans.setDescription("Salary Advance Init");
            comTransRepo.saveAndFlush(comTrans);

            //  2. Tạo bản ghi chi tiết — ID = comTrans.getId() 
            ComTransDtlLmt tempRecord = new ComTransDtlLmt();
            tempRecord.setId(comTrans.getId());

            tempRecord.setHostCifId(custInfo.getHostCifId());
            tempRecord.setFullName(emCustInfo.getFamilyName() + " " + emCustInfo.getFirstName());
            tempRecord.setNationalId(custInfo.getIdTypNo());
            tempRecord.setGender(emCustInfo.getGender());

            if (!Utility.isNull(emCustInfo.getDateOfBirth())) {
                try {
                    tempRecord.setDateOfBirth(sdf.parse(emCustInfo.getDateOfBirth()));
                } catch (Exception e) {
                    AppLog.warning("[SA INIT] Cannot parse dateOfBirth: " + emCustInfo.getDateOfBirth());
                }
            }

            tempRecord.setNationality(emCustInfo.getNationality());

            if (custT24 != null && !Utility.isNull(custT24.getMaritalStatus())) {
                tempRecord.setMaritalStatus(custT24.getMaritalStatus());
            }

            if (custT24 != null && custT24.getContactInfo() != null
                    && custT24.getContactInfo().getAddress() != null
                    && !custT24.getContactInfo().getAddress().isEmpty()) {

                com.mbc.common.services.il.customerinfo.CustomerAddress custAddress = custT24.getContactInfo().getAddress().get(0);
                for (com.mbc.common.services.il.customerinfo.CustomerAddress addr : custT24.getContactInfo().getAddress()) {
                    if ("Current".equalsIgnoreCase(addr.getAddressTypeCode())) {
                        custAddress = addr;
                        break;
                    }
                }
                tempRecord.setAddressProvince(custAddress.getProvinceCode());
                tempRecord.setAddressDistrict(custAddress.getDistrictCode());
                tempRecord.setAddressWard(custAddress.getWardCode());
            }

            tempRecord.setOccupation(emCustInfo.getCurrentOccupation());
            tempRecord.setCompanyName(emCustInfo.getCompanyName());

            if (emSalaryInfo != null) {
                tempRecord.setMonthlyIncome(emSalaryInfo.getSalary3mAvgUSD());
                tempRecord.setMonthlySalaryAmountUsd(emSalaryInfo.getSalary3mAvgUSD());
            }

            tempRecord.setLoanType(LOAN_TYPE_SALARY_ADVANCE);
            tempRecord.setStep(STEP_CHECK_CUST);
            tempRecord.setStatus(STATUS_SUCCESS);

            comTransDtlLmtRepo.saveAndFlush(tempRecord);

            //  3. Tạo ComTransProcess để track giao dịch 
            ComTransProcess comTransProcess = new ComTransProcess();
            comTransProcess.setTransId(comTrans.getId());
            comTransProcess.setSrvcCd(request.getSrvcCd());
            comTransProcess.setStatus(Constant.COM_STATUS_INT);
            comTransProcessRepo.saveAndFlush(comTransProcess);

            // Put tempRecordId vào context
            context.put("tempRecordId", comTrans.getId());

            AppLog.info("[SA INIT - SAVE TEMP RECORD] Saved - id: " + comTrans.getId());

        } catch (Exception e) {
            AppLog.error("[Exception Save SA Temp Record] requestId: " + request.getRequestId() + " desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
