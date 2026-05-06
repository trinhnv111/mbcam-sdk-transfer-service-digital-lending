package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.digitalloan.output.MsLoanGetPdOutput;
import com.mbc.mobileapp.api.model.digitalloan.output.PaymentHistoryOutPut;
import com.mbc.mobileapp.api.model.salary_advance.input.MsLoanCalculateLimitRequest;
import com.mbc.mobileapp.api.model.salary_advance.output.MsLoanCalculateLimitResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@Slf4j
public class ApiMsLoan extends CallMicroService {

    @Value("${api.ms-loan.calculate-limit.url:}")
    private String calculateLimitUrl;

    @Value("${api.ms-loan.mock.enabled:true}")
    private boolean mockEnabled;


    public ApiMsLoan() {
    }

    /**
     * @param hostCifId
     * @param custId
     * @param requestId
     * @return
     */
    public ExecuteT24Output<Object> getLoan(String hostCifId, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        String url = getUrl("microservice.ms-loan.host") + getUrl("microservice.ms-loan.get-loan");
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url).queryParam("customerCode", hostCifId);
        ExecuteT24Output<Object> output = getForMicroService(uri.build().toUri(), headers, new ParameterizedTypeReference<ExecuteT24Output<Object>>() {
        });
        mappingErrorCode(output);
        return output;
    }

    /**
     * @param hostcifId
     * @param custId
     * @param requestId
     * @return
     */
    public ExecuteT24Output<MsLoanGetPdOutput> getPd(String hostcifId, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        String url = getUrl("microservice.ms-loan.host") + getUrl("microservice.ms-loan.get-pd");
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url).queryParam("customerCode", hostcifId);
        ExecuteT24Output<MsLoanGetPdOutput> output = getForMicroService(uri.build().toUri(), headers, new ParameterizedTypeReference<ExecuteT24Output<MsLoanGetPdOutput>>() {
        });
        mappingErrorCode(output);
        return output;
    }

    /**
     * @param loanId
     * @param fromDate
     * @param toDate
     * @param custId
     * @param requestId
     * @return
     */
    public ExecuteT24Output<List<PaymentHistoryOutPut>> getPaymentHistory(String loanId, String fromDate, String toDate, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        String url = getUrl("microservice.ms-loan.host") + getUrl("microservice.ms-loan.get-payment-history");
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url).queryParam("ldId", loanId).queryParam("fromDate", fromDate).queryParam("toDate", toDate);
        ExecuteT24Output<List<PaymentHistoryOutPut>> output = getForMicroService(uri.build().toUri(), headers, new ParameterizedTypeReference<ExecuteT24Output<List<PaymentHistoryOutPut>>>() {
        });
        mappingErrorCode(output);
        return output;
    }


    /**
     * Gọi MS Loan API tính toán hạn mức.
     * Nếu mock.enabled=true hoặc URL rỗng → trả mock response.
     */
    public MsLoanCalculateLimitResponse calculateLimit(MsLoanCalculateLimitRequest request,
                                                       String custId, String requestId) {
        if (mockEnabled || Utility.isNull(calculateLimitUrl)) {
            log.info("[ApiMsLoan] Mock mode — returning mock limit data for customerCode: {}",
                    request.getCustomerCode());
            return buildMockResponse(request.getCustomerCode());
        }

        try {
            String msgId = Utility.getUUID();
            HttpHeaders headers = buildHeader(custId, requestId, msgId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<MsLoanCalculateLimitRequest> entity = new HttpEntity<>(request, headers);

            log.info("[ApiMsLoan] Calling MS Loan calculate-limit - url: {}, customerCode: {}",
                    calculateLimitUrl, request.getCustomerCode());

            ResponseEntity<MsLoanCalculateLimitResponse> response = restTemplate.exchange(
                    calculateLimitUrl,
                    HttpMethod.POST,
                    entity,
                    MsLoanCalculateLimitResponse.class
            );

            MsLoanCalculateLimitResponse body = response.getBody();
            log.info("[ApiMsLoan] MS Loan response - status: {}, limitAmount: {}",
                    body != null ? body.getStatus() : "null",
                    body != null && body.getData() != null ? body.getData().getLimitAmount() : "null");

            return body;

        } catch (Exception e) {
            log.error("[ApiMsLoan] Exception calling MS Loan calculate-limit: ", e);
            return null;
        }
    }

    /**
     * Mock response
     */
    private MsLoanCalculateLimitResponse buildMockResponse(String customerCode) {
        java.time.LocalDate today = java.time.LocalDate.now();

        MsLoanCalculateLimitResponse.LimitData limitData = MsLoanCalculateLimitResponse.LimitData.builder()
                .customerCode(customerCode)
                .limitAmount(new java.math.BigDecimal("750.00"))
                .limitCurrency("USD")
                .limitValueDate(today.toString())
                .limitEndDate(today.plusMonths(3).toString())
                .build();

        return MsLoanCalculateLimitResponse.builder()
                .status("200")
                .error("Success")
                .clientMessageId("MOCK-" + Utility.getUUID())
                .data(limitData)
                .build();
    }


}

