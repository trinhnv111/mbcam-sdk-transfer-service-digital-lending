//package com.mbc.mobileapp.process;
//
//import com.mbc.common.entity.ComTrans;
//import com.mbc.common.entity.ComTransDtlLmt;
//import com.mbc.common.il.base.ExecuteT24Output;
//import com.mbc.common.repository.ComTransDtlLmtRepository;
//import com.mbc.common.repository.ComTransRepo;
//import com.mbc.common.util.Constant;
//import com.mbc.common.util.Utility;
//import com.mbc.mobileapp.api.ApiCBC;
//import com.mbc.mobileapp.api.ApiMsLoan;
//import com.mbc.mobileapp.api.model.digitalloan.output.MsLoanGetPdOutput;
//import com.mbc.mobileapp.api.model.digitalloan.output.PdData;
//import com.mbc.mobileapp.api.model.salary_advance.input.MsLoanCalculateLimitRequest;
//import com.mbc.mobileapp.api.model.salary_advance.output.EmSalaryInfo;
//import com.mbc.mobileapp.api.model.salary_advance.output.MsLoanCalculateLimitResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.temporal.ChronoUnit;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class SalaryAdvanceRetryProcess {
//    private static final String LOAN_TYPE_SALARY_ADVANCE = "SALARY_ADVANCE";
//
//    private final ComTransDtlLmtRepository comTransDtlLmtRepo;
//    private final ComTransRepo comTransRepo;
//    private final ApiCBC apiCBC;
//    private final ApiMsLoan apiMsLoan;
//    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
//
//    @Value("${api.cbc.requestBy}")
//    private String cbcRequestBy;
//
//    @Value("${api.cbc.appCode}")
//    private String cbcAppCode;
//
//    // Chạy ngầm định kỳ 15 phút 1 lần
//    @Scheduled(fixedDelay = 900000)
//    public void retryPendingSalaryAdvance() {
//        log.info("[SalaryAdvanceRetryProcess] Start scanning PND records...");
//
//        try {
//            // Tại 1 thời điểm chỉ có 1 khoản vay duy nhất
//            ComTransDtlLmt record = comTransDtlLmtRepo.findTopByLoanTypeAndStatusOrderByCreatedAtDesc(LOAN_TYPE_SALARY_ADVANCE, Constant.COM_STATUS_PND);
//
//            if (record == null) {
//                log.info("[SalaryAdvanceRetryProcess] No PND record found. Skip.");
//                return;
//            }
//
//            // Check thời gian tạo (giới hạn 24h)
//            Date createdAt = record.getCreatedAt();
//            if (createdAt == null) return;
//
//            LocalDateTime createdTime = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//            long hoursPassed = ChronoUnit.HOURS.between(createdTime, LocalDateTime.now());
//
//            if (hoursPassed >= 24) {
//                log.info("[SalaryAdvanceRetryProcess] Record {} exceeded 24h. Marking as FAIL/TIMEOUT.", record.getId());
//                updateStatus(record.getId(), Constant.COM_STATUS_FAIL);
//                // TODO: Gọi API Noti báo cho KH biết luồng bị Fail do quá 24h, KH cần làm lại
//                return;
//            }
//
//            log.info("[SalaryAdvanceRetryProcess] Retrying for record: {}", record.getId());
//            processRetry(record);
//
//        } catch (Exception e) {
//            log.error("[SalaryAdvanceRetryProcess] Error during retry process", e);
//        }
//    }
//
//    private void processRetry(ComTransDtlLmt record) {
//        try {
//            String hostCifId = record.getHostCifId();
//            String idNumber = record.getNationalId();
//            String transId = record.getId();
//
//            //Job chạy ngầm không có context request của HTTP ->  dùng lại transId làm refNo
//            String clientMessageId = transId;
//
//            //  Cần thêm userId và custId vào ComTransDtlLmt hoặc query từ bảng khác
//            // Tạm truyền rỗng hoặc mock do limit API hiện tại
//            String clientUserId = "";
//            String custId = "";
//
//            // Retry CBC
//            ExecuteT24Output<List<Map<String, Object>>> cbcResponse = apiCBC.getCbcData(
//                    clientMessageId, clientUserId, cbcRequestBy, cbcAppCode, Collections.singletonList(idNumber));
//
//            if (cbcResponse == null || !Constant.CALL_MICROSERVICE_SUCCESS.equals(cbcResponse.getStatus())) {
//                log.warn("[SalaryAdvanceRetryProcess] Retry CBC failed for {}", transId);
//                return; // Continue pending, wait for next retry
//            }
//
//            Map<String, Object> cbcData = org.apache.commons.collections.CollectionUtils.isEmpty(cbcResponse.getData())
//                    ? null : cbcResponse.getData().get(0);
//            String historyOneYear = cbcData == null ? null : (String) cbcData.get("historyOneYear");
//
//            if (Utility.isNull(historyOneYear)) {
//                return ; // Wait next retry
//            }
//
//            String history = historyOneYear.toLowerCase().trim();
//            if (!("normal".equals(history) || "closed".equals(history) || "reject".equals(history) || "no information".equals(history))) {
//                // Có nợ xấu CBC -> Failed luôn
//                updateStatus(transId, Constant.COM_STATUS_FAIL);
//                // TODO: Bắn Noti báo fail do CBC
//                return;
//            }
//
//            // Retry PD
//            ExecuteT24Output<MsLoanGetPdOutput> pdResponse = apiMsLoan.getPd(hostCifId, custId, transId);
//            if (pdResponse == null || !Constant.CALL_MICROSERVICE_SUCCESS.equals(pdResponse.getStatus())) {
//                log.warn("[SalaryAdvanceRetryProcess] Retry PD failed for {}", transId);
//                return; // Wait next retry
//            }
//
//            MsLoanGetPdOutput pdOutput = pdResponse.getData();
//            if (pdOutput != null && !CollectionUtils.isEmpty(pdOutput.getPdLdList())) {
//                Optional<PdData> badDebtRecord = pdOutput.getPdLdList().stream()
//                        .filter(this::hasBadDebt)
//                        .findFirst();
//
//                if (badDebtRecord.isPresent()) {
//                    // Có nợ xấu PD -> Failed luôn
//                    updateStatus(transId, Constant.COM_STATUS_FAIL);
//                    // TODO: Bắn Noti báo fail do PD
//                    return;
//                }
//            }
//
//            // Nếu qua hết -> Gọi API tính Limit (MS LOAN)
//            log.info("[SalaryAdvanceRetryProcess] CBC and PD passed. Calling MS Loan to calculate Limit...");
//
//            EmSalaryInfo emSalaryInfo = null;
//            if (!Utility.isNull(record.getSalaryInfoDetail())) {
//                try {
//                    emSalaryInfo = objectMapper.readValue(record.getSalaryInfoDetail(), EmSalaryInfo.class);
//                } catch (Exception e) {
//                    log.error("[SalaryAdvanceRetryProcess] Failed to parse SalaryInfoDetail", e);
//                }
//            }
//
//            MsLoanCalculateLimitRequest msRequest = buildMsLoanRequest(hostCifId, emSalaryInfo);
//            MsLoanCalculateLimitResponse msResponse = apiMsLoan.calculateLimit(msRequest, custId, clientMessageId);
//
//            if (msResponse == null || msResponse.getData() == null) {
//                log.warn("[SalaryAdvanceRetryProcess] Failed to calculate limit for {}", transId);
//                return; // Wait next retry
//            }
//
//            MsLoanCalculateLimitResponse.LimitData limitData = msResponse.getData();
//            BigDecimal approveLmt = limitData.getLimitAmount();
//            String limitCurrency = limitData.getLimitCurrency();
//
//            // Sau khi có limit -> Update SUCCESS
//            updateStatusWithLimit(transId, Constant.COM_STATUS_COM, approveLmt, limitCurrency);
//
//            // TODO: Bắn Noti báo thành công cho KH
//            log.info("[SalaryAdvanceRetryProcess] Successfully approved Limit for {} - Amount: {}", transId, approveLmt);
//
//        } catch (Exception e) {
//            log.error("[SalaryAdvanceRetryProcess] Exception retrying record {}", record.getId(), e);
//        }
//    }
//
//    private void updateStatusWithLimit(String transId, String status, BigDecimal limit, String currency) {
//        Optional<ComTrans> comTransOpt = comTransRepo.findById(transId);
//        comTransOpt.ifPresent(comTrans -> {
//            comTrans.setStatus(status);
//            comTransRepo.saveAndFlush(comTrans);
//        });
//
//        Optional<ComTransDtlLmt> tempRecordOpt = comTransDtlLmtRepo.findById(transId);
//        tempRecordOpt.ifPresent(tempRecord -> {
//            tempRecord.setStatus(status);
//            if (limit != null) {
//                tempRecord.setLoanLimit(limit);
//                tempRecord.setApproveLimit(limit);
//                tempRecord.setUsedLimit(BigDecimal.ZERO);
//                tempRecord.setRemaining(limit);
//                tempRecord.setCurrency(currency);
//
//                Date now = new Date();
//                tempRecord.setStartDate(now);
//                java.util.Calendar cal = java.util.Calendar.getInstance();
//                cal.setTime(now);
//                cal.add(java.util.Calendar.DAY_OF_YEAR, 365);
//                tempRecord.setEndDate(cal.getTime());
//            }
//            comTransDtlLmtRepo.saveAndFlush(tempRecord);
//        });
//    }
//
//    private void updateStatus(String transId, String status) {
//        updateStatusWithLimit(transId, status, null, null);
//    }
//
//    private boolean hasBadDebt(PdData pd) {
//        double prAmt = parseAmount(pd.getPrAmt());
//        double inAmt = parseAmount(pd.getInAmt());
//        double peAmt = parseAmount(pd.getPeAmt());
//        return prAmt > 0 || inAmt > 0 || peAmt > 0;
//    }
//
//    private double parseAmount(String amtStr) {
//        if (Utility.isNull(amtStr)) return 0;
//        try {
//            return Double.parseDouble(amtStr);
//        } catch (NumberFormatException e) {
//            return 0;
//        }
//    }
//
//    private MsLoanCalculateLimitRequest buildMsLoanRequest(String hostCifId, EmSalaryInfo salary) {
//        List<MsLoanCalculateLimitRequest.SalaryMonth> salaryMonths = new ArrayList<>();
//
//        if (salary != null) {
//            salaryMonths.add(buildSalaryMonth(salary.getSalaryAmountT1USD(), salary.getSalaryAmountT1KHR()));
//            salaryMonths.add(buildSalaryMonth(salary.getSalaryAmountT2USD(), salary.getSalaryAmountT2KHR()));
//            salaryMonths.add(buildSalaryMonth(salary.getSalaryAmountT3USD(), salary.getSalaryAmountT3KHR()));
//        }
//
//        return MsLoanCalculateLimitRequest.builder()
//                .customerCode(hostCifId)
//                .channel("SDK")
//                .product("DIGITAL_LOAN")
//                .subProduct("SALARY_ADVANCE")
//                .partnerCode("EMONEY")
//                .limitCurrency("USD")
//                .salary(salaryMonths)
//                .build();
//    }
//
//    private MsLoanCalculateLimitRequest.SalaryMonth buildSalaryMonth(BigDecimal usd, BigDecimal khr) {
//        List<MsLoanCalculateLimitRequest.SalaryDetail> details = new ArrayList<>();
//        if (usd != null) details.add(MsLoanCalculateLimitRequest.SalaryDetail.builder().salaryAmount(usd).salaryCurrency("USD").build());
//        if (khr != null) details.add(MsLoanCalculateLimitRequest.SalaryDetail.builder().salaryAmount(khr).salaryCurrency("KHR").build());
//        return MsLoanCalculateLimitRequest.SalaryMonth.builder().salaryDetail(details).build();
//    }
//}
