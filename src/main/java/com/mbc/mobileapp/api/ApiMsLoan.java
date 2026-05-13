package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.digitalloan.output.GetLoanOutput;
import com.mbc.mobileapp.api.model.digitalloan.output.MsLoanGetPdOutput;
import com.mbc.mobileapp.api.model.digitalloan.output.PaymentHistoryOutPut;
import com.mbc.mobileapp.api.model.salary_advance.input.MsLoanOfferLimitRequest;
import com.mbc.mobileapp.api.model.salary_advance.output.MsLoanOfferLimitResponse;
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

    @Value("${microservice.ms-loan.offer-limit:/loan/v1.0/offer-limit}")
    private String offerLimitPath;
    public ApiMsLoan() {
    }

    /**
     * @param hostCifId
     * @param custId
     * @param requestId
     * @return
     */
    public ExecuteT24Output<GetLoanOutput> getLoan(String hostCifId, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        String url = getUrl("microservice.ms-loan.host") + getUrl("microservice.ms-loan.get-loan");
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url).queryParam("customerCode", hostCifId);
        ExecuteT24Output<GetLoanOutput> output = getForMicroService(uri.build().toUri(), headers,
                new ParameterizedTypeReference<ExecuteT24Output<GetLoanOutput>>() {});
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
     */
    public MsLoanOfferLimitResponse offerLimit(MsLoanOfferLimitRequest request,
                                               String custId, String requestId) {
        try {
            String msgId = Utility.getUUID();
            HttpHeaders headers = buildHeader(custId, requestId, msgId);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String host = getUrl("microservice.ms-loan.host");
            String path = getUrl("microservice.ms-loan.offer-limit");
            if (Utility.isNull(path)) {
                path = offerLimitPath;
            }
            String url = host + path;

            log.info("[ApiMsLoan] Calling MS Loan offer-limit - url: {}, customerCode: {}",
                    url, request.getCustomerCode());

            MsLoanOfferLimitResponse body = postForMicroService(
                    url,
                    headers,
                    request,
                    new ParameterizedTypeReference<MsLoanOfferLimitResponse>() {}
            );
            log.info("[ApiMsLoan] MS Loan response - status: {}, limitAmount: {}",
                    body != null ? body.getStatus() : "null",
                    body != null && body.getData() != null ? body.getData().getLimitAmount() : "null");

            return body;

        } catch (Exception e) {
            log.error("[ApiMsLoan] Exception calling MS Loan offer-limit: ", e);
            return null;
        }
    }



}

