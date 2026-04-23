package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepo;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.AppLog;
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
 * Data sources:
 *   - EmCustomerInfo (Nhóm 1 eMoney): gender, nationality, dateOfBirth, occupation, companyName
 *   - EmSalaryInfo (Nhóm 2 eMoney): salary3mAvgUSD
 *   - CustomerInfoT24 (MS Customer): fullName
 *   - CustInfo (session): hostCifId, nationalId (idTypNo)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoSavaSalaryAdvanceTemRecord implements Command {

    private static final String LOAN_TYPE_SALARY_ADVANCE = "SALARY_ADVANCE";
    private static final String STEP_CHECK_CUST = "CHECK_CUST";
    private static final String STATUS_SUCCESS = "Success";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final ComTransDtlLmtRepo comTransDtlLmtRepo;

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

            ComTransDtlLmt tempRecord = new ComTransDtlLmt();

            // ID (UUID)
            tempRecord.setId(Utility.getUUID());

            // host_cif_id (từ session)
            tempRecord.setHostCifId(custInfo.getHostCifId());

            // fullName: ưu tiên T24 engName, fallback eMoney familyName + firstName
            if (custT24 != null && custT24.getCustomerName() != null
                    && !Utility.isNull(custT24.getCustomerName().getEngName())) {
                tempRecord.setFullName(custT24.getCustomerName().getEngName());
            } else {
                tempRecord.setFullName(emCustInfo.getFamilyName() + " " + emCustInfo.getFirstName());
            }

            // nationalId (từ session — T24)
            tempRecord.setNationalId(custInfo.getIdTypNo());

            // gender (từ eMoney: MALE / FEMALE / OTHER)
            tempRecord.setGender(emCustInfo.getGender());

            // date_of_birth (từ eMoney)
            if (!Utility.isNull(emCustInfo.getDateOfBirth())) {
                try {
                    tempRecord.setDateOfBirth(sdf.parse(emCustInfo.getDateOfBirth()));
                } catch (Exception e) {
                    AppLog.warn("[SA INIT] Cannot parse dateOfBirth: " + emCustInfo.getDateOfBirth());
                }
            }

            // nationality (từ eMoney: KH / VN)
            tempRecord.setNationality(emCustInfo.getNationality());

            // marital_status (từ T24 — eMoney không có field này)
            if (custT24 != null && !Utility.isNull(custT24.getMaritalStatus())) {
                tempRecord.setMaritalStatus(custT24.getMaritalStatus());
            }

            // address (từ T24 customerAddress, fallback eMoney residential string)
            if (custT24 != null && custT24.getCustomerAddress() != null) {
                tempRecord.setAddressProvince(custT24.getCustomerAddress().getProvinceCode());
                tempRecord.setAddressDistrict(custT24.getCustomerAddress().getDistrictCode());
                tempRecord.setAddressWard(custT24.getCustomerAddress().getWardCode());
            }

            // occupation (từ eMoney)
            tempRecord.setOccupation(emCustInfo.getCurrentOccupation());

            // company_name (từ eMoney)
            tempRecord.setCompanyName(emCustInfo.getCompanyName());

            // employment_start_date → eMoney không có, skip

            // monthly_income (từ salaryInfo.salary3mAvgUSD)
            if (emSalaryInfo != null) {
                tempRecord.setMonthlyIncome(emSalaryInfo.getSalary3mAvgUSD());
                tempRecord.setMonthlySalaryAmountUsd(emSalaryInfo.getSalary3mAvgUSD());
            }

            // loan_type
            tempRecord.setLoanType(LOAN_TYPE_SALARY_ADVANCE);

            // step + status
            tempRecord.setStep(STEP_CHECK_CUST);
            tempRecord.setStatus(STATUS_SUCCESS);

            comTransDtlLmtRepo.saveAndFlush(tempRecord);

            // Put tempRecordId vào context
            context.put("tempRecordId", tempRecord.getId());

            AppLog.info("[SA INIT - SAVE TEMP RECORD] Saved - id: " + tempRecord.getId());

        } catch (Exception e) {
            AppLog.error("[Exception Save SA Temp Record] requestId: " + request.getRequestId() + " desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
