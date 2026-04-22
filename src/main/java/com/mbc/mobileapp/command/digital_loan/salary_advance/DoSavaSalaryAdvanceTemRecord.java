package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.repository.ComTransDtlLmtRepo;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.mobileapp.api.model.salary_advance.output.EmCustInfoOutput;
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

public class DoSavaSalaryAdvanceTemRecord implements Command {
    //    Save bản ghi tạm vào COM_LOAN_DISBUR_LMT
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
            EmCustInfoOutput emCustInfo = (EmCustInfoOutput) context.get("emCustInfoOutput");
            CustomerInfoT24 customerInfoT24 = (CustomerInfoT24) context.get("customerInfoMS");
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

            ComTransDtlLmt tempRecord = new ComTransDtlLmt();

            //  ID (UUID)
            tempRecord.setId(Utility.getUUID());

            // host_cif_id (từ session)
            tempRecord.setHostCifId(custInfo.getHostCifId());

            //  fullName: ưu tiên T24, fallback eMoney
            if (customerInfoT24 != null && customerInfoT24.getCustomerName() != null
                    && !Utility.isNull(customerInfoT24.getCustomerName().getEngName())) {
                tempRecord.setFullName(customerInfoT24.getCustomerName().getEngName());
            } else {
                tempRecord.setFullName(emCustInfo.getEnglishName());
            }

            //  nationalId (từ session — T24)
            tempRecord.setNationalId(custInfo.getIdTypNo());

            //   gender (từ eMoney)
            tempRecord.setGender(emCustInfo.getGender());

            //   date_of_birth (từ eMoney)
            if (!Utility.isNull(emCustInfo.getDateOfBirth())) {
                try {
                    tempRecord.setDateOfBirth(sdf.parse(emCustInfo.getDateOfBirth()));
                } catch (Exception e) {
                    AppLog.warn("[SA INIT] Cannot parse dateOfBirth: " + emCustInfo.getDateOfBirth());
                }
            }

            //  nationality (từ eMoney)
            tempRecord.setNationality(emCustInfo.getNationality());
            

            //   marital_status (từ eMoney)
            tempRecord.setMaritalStatus(emCustInfo.getMaritalStatus());

            //   address_province (từ eMoney - nơi cư trú)
            tempRecord.setAddressProvince(emCustInfo.getResidentialProvince());

            //   address_district (từ eMoney)
            tempRecord.setAddressDistrict(emCustInfo.getResidentialDistrict());

            //   address_ward (từ eMoney)
            tempRecord.setAddressWard(emCustInfo.getResidentialCommune());

            //   occupation (từ eMoney)
            tempRecord.setOccupation(emCustInfo.getCurrentOccupation());

            //   company_name (từ eMoney)
            tempRecord.setCompanyName(emCustInfo.getCompanyName());

            //   employment_start_date (từ eMoney)
            if (!Utility.isNull(emCustInfo.getEmploymentDate())) {
                try {
                    tempRecord.setEmploymentStartDate(sdf.parse(emCustInfo.getEmploymentDate()));
                } catch (Exception e) {
                    AppLog.warn("[SA INIT] Cannot parse employmentDate: " + emCustInfo.getEmploymentDate());
                }
            }

            //   monthly_income (từ eMoney - monthlySalaryAmountUsd)
            tempRecord.setMonthlyIncome(emCustInfo.getMonthlySalaryAmountUsd());

            //   loan_type
            tempRecord.setLoanType(LOAN_TYPE_SALARY_ADVANCE);
            

            //   step
            tempRecord.setStep(STEP_CHECK_CUST);

            //   status (bước Check Cust thành công)
            tempRecord.setStatus(STATUS_SUCCESS);
            

            //   monthly_salary_amount_USD (từ eMoney)
            tempRecord.setMonthlySalaryAmountUsd(emCustInfo.getMonthlySalaryAmountUsd());
            
            comTransDtlLmtRepo.saveAndFlush(tempRecord);

            
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
