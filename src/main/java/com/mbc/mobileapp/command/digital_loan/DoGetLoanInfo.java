package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.entity.ComTransDtlLoanRegistration;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.repository.ComTransDtlLoanRegistrationRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.SalaryAdvanceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.digitalloan.disbursement.*;
import com.mbc.mobileapp.rest.digitalloan.repayment.LoanRepaymentRequest;
import com.mbc.mobileapp.service.digital_loan.LoanDisbursementInformation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoGetLoanInfo implements Command {

    private final ComTransDtlLmtRepository comTransDtlLmtRepository;
    private final ComTransDtlLoanRegistrationRepo comTransDtlLoanRegistrationRepository;
    private final ComTransRepo comTransRepo;
    private final ComTransProcessRepo comTransProcessRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();

        try {
            ComTransDtlLmt comTransDtlLmt = comTransDtlLmtRepository.
                    findTopByHostCifIdAndLoanTypeAndStatusOrderByCreatedAtDesc(custInfo.getHostCifId(),
                            SalaryAdvanceConstant.LOAN_TYPE_SALARY_ADVANCE, Constant.STATUS_SUCCESS);
            if (comTransDtlLmt == null) {
                log.info("[LOAN INFO] is null requestId:{}",request.getRequestId());

                return true;
            }
            DisbursementInformationRequest disbursementInformationRequest = request.getDigitalLoanRequest();
            BigDecimal referAmount = new BigDecimal(disbursementInformationRequest.getReferLoanAmount()) ;

            BigDecimal fee = context.getVar("fee", BigDecimal.class);
            Double feeDouble = context.getVar("fee", Double.class);
            if (feeDouble == null) {
                log.error("[LOAN INFO] fee is null in context, requestId: {}", request.getRequestId());
                result = new SimpleResult("Không tìm thấy phí", false, ResponseCode.TRANSACTION_FAIL.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            BigDecimal feeB = BigDecimal.valueOf(feeDouble);
            BigDecimal loanfee = referAmount.multiply(feeB);

            // ── ComTrans (transaction header) ──────────────────────────
            ComTrans comTrans = new ComTrans();
            comTrans.setSessionId(request.getSessionId());
            comTrans.setCustId(custInfo.getId());
            comTrans.setCreatedBy(custInfo.getUserId());
            comTrans.setStatus(Constant.COM_STATUS_INT);
            comTrans.setSrvcCd(request.getSrvcCd());
            comTrans.setTransferType("INHOUSE");
            comTrans.setTransactionType("INHOUSE");
            comTransRepo.saveAndFlush(comTrans);

            // ── ComTransProcess ─────────────────────────────────────────
            ComTransProcess comTransProcess = new ComTransProcess();
            comTransProcess.setStatus(Constant.COM_STATUS_INT);
            comTransProcess.setTransId(comTrans.getId());
            comTransProcess.setSrvcCd(comTrans.getSrvcCd());
            comTransProcessRepo.saveAndFlush(comTransProcess);

            // ── ComTransDtlLoanRegistration ─────────────────────────────
            ComTransDtlLoanRegistration comTransDtlLoanRegistration = new ComTransDtlLoanRegistration();
            comTransDtlLoanRegistration.setId(comTrans.getId()); // dùng comTrans.id làm PK
            comTransDtlLoanRegistration.setHostCifId(comTransDtlLmt.getHostCifId());
            comTransDtlLoanRegistration.setCustomerName(comTransDtlLmt.getFullName());
            comTransDtlLoanRegistration.setMaritalStatus(comTransDtlLmt.getMaritalStatus());
            comTransDtlLoanRegistration.setEmploymentStartDate(comTransDtlLmt.getEmploymentStartDate());
            comTransDtlLoanRegistration.setAddressProvince(comTransDtlLmt.getAddressProvince());
            comTransDtlLoanRegistration.setAddressDistrict(comTransDtlLmt.getAddressDistrict());
            comTransDtlLoanRegistration.setAddressWard(comTransDtlLmt.getAddressWard());
            comTransDtlLoanRegistration.setAccountCurrency(comTransDtlLmt.getCurrency());
            comTransDtlLoanRegistration.setStatus(Constant.COM_STATUS_INT);
            comTransDtlLoanRegistration.setStep("GET-FEE");
            comTransDtlLoanRegistration.setNationalId(comTransDtlLmt.getNationalId());
            comTransDtlLoanRegistration.setGender(comTransDtlLmt.getGender());
            comTransDtlLoanRegistration.setLoanDueDate(comTransDtlLmt.getEndDate());
            comTransDtlLoanRegistration.setRefpartnerCode(disbursementInformationRequest.getDisbursementAccountType());
            comTransDtlLoanRegistration.setLoanAmount(referAmount);
            comTransDtlLoanRegistration.setReferrerPhone(disbursementInformationRequest.getReferrerPhone());
            comTransDtlLoanRegistration.setDisbursementAccount(disbursementInformationRequest.getDisbursementAccountType());
            comTransDtlLoanRegistrationRepository.saveAndFlush(comTransDtlLoanRegistration);

            // Lưu transId vào context để genFile dùng
            context.put("loanRegistrationId", comTrans.getId());
            log.info("[LOAN INFO] Saved registration - id:{}, requestId:{}", comTrans.getId(), request.getRequestId());


            CustomerInformation customerInfo = new CustomerInformation();
            customerInfo.setIdNumber(comTransDtlLmt.getNationalId());
            customerInfo.setFullName(comTransDtlLmt.getFullName());
            customerInfo.setMaritalStatus(comTransDtlLmt.getMaritalStatus());
            customerInfo.setEmploymentStartDate(String.valueOf(comTransDtlLmt.getEmploymentStartDate()));
            customerInfo.setEmail(comTransDtlLmt.getEmail());
            customerInfo.setPhoneNumber(comTransDtlLmt.getPhoneNumber());

            String currentAddress = String.format("%s, %s, %s",
                    comTransDtlLmt.getAddressWard(),
                    comTransDtlLmt.getAddressDistrict(),
                    comTransDtlLmt.getAddressProvince());
            customerInfo.setCurrentAddress(currentAddress);

            String address = String.format("%s, %s, %s",
                    comTransDtlLmt.getPlaceOfBirthProvince(),
                    comTransDtlLmt.getPlaceOfBirthDistrict(),
                    comTransDtlLmt.getPlaceOfBirthWard());
            customerInfo.setPlaceOfBrith(address);


            BigDecimal receivingAmount = referAmount.subtract(loanfee); // 900 - 2 = 898

            LoanInformation loanInfo = new LoanInformation();
            loanInfo.setReferLoanLimit(referAmount.toPlainString());          // 900.00
            loanInfo.setFee(loanfee.toPlainString());                         // 2.00 (số tiền phí thực tế)
            loanInfo.setReceivingAmount(receivingAmount.toPlainString());     // 898.00
            loanInfo.setDueDate(String.valueOf(comTransDtlLmt.getEndDate()));

            // 3. Đóng gói vào DisburseInfData
            DisburseInfData disburseInfData = new DisburseInfData();
            disburseInfData.setCustomerInformation(customerInfo);
            disburseInfData.setLoanInformation(loanInfo);

            DisbursementInformationResponse disbursementResponse = new DisbursementInformationResponse();
            disbursementResponse.setTransId(comTrans.getId()); // FE dùng để gửi lên /genfile
            disbursementResponse.setData(disburseInfData);

            response.setDisbursementInformationResponse(disbursementResponse);

        } catch (Exception e) {
            log.error("[EXCEPTION GET OD LOAN] requestId: {}, desc: ", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }
}
