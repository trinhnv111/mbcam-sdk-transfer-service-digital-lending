package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.digitalloan.input.MsLoanCreateRequest;
import com.mbc.mobileapp.api.model.digitalloan.output.*;
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

//    @Value("${microservice.ms-loan.offer-limit:/loan/v1.0/offer-limit}")
//    private String offerLimitPath;
    public ApiMsLoan() {
    }

    /**
     * @param hostCifId
     * @param custId
     * @param requestId
     * @return
     */
    public ExecuteT24Output<GetLoanOutput> getLoan(String hostCifId, String ldId, String accountNo, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        String url = getUrl("microservice.ms-loan.host") + getUrl("microservice.ms-loan.get-loan");
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url).queryParam("customerCode", hostCifId);
        if (!Utility.isNull(ldId)) {
            uri.queryParam("ldID", ldId);
        }
        if (!Utility.isNull(accountNo)) {
            uri.queryParam("accountNo", accountNo);
        }
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
//            if (Utility.isNull(path)) {
//                path = offerLimitPath;
//            }
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

    /**
     * Gọi MS Loan API tính phí.
     */
    public ExecuteT24Output<LdFeeData> getLdFee(String custId, String requestId) {
        String messageId = Utility.getUUID();

        String channel ="SDK.RETAIL";
        String product = "DIGITAL_LOAN";
        String subProduct ="SALARY_ADVANCE";
        String partnerCode ="EMONEY";

        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        String url = getUrl("microservice.ms-loan.host") + getUrl("microservice.ms-loan.ld-fee");

        LdFeeRequest requestBody = LdFeeRequest.builder()
                .channel(channel)
                .product(product)
                .subProduct(subProduct)
                .partnerCode(partnerCode)
                .build();


        ExecuteT24Output<LdFeeData> output = postForMicroService(
                url,
                headers,
                requestBody,
                new ParameterizedTypeReference<ExecuteT24Output<LdFeeData>>() {}
        );

        mappingErrorCode(output);
        return output;
    }

    /**
     * Gọi MS Loan API thực hiện giải ngân .
     */

    public ExecuteT24Output<MsLoanCreateOutput> createLoan(MsLoanCreateRequest request,
                                                           String custId, String requestId) {
        try {
            String msgId = Utility.getUUID();
            HttpHeaders headers = buildHeader(custId, requestId, msgId);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = getUrl("microservice.ms-loan.host") + getUrl("microservice.ms-loan.create-loan");
            log.info("[ApiMsLoan] createLoan - url:{}, customerCode:{}", url, request.getCustomerCode());
            ExecuteT24Output<MsLoanCreateOutput> output = postForMicroService(
                    url, headers, request,
                    new ParameterizedTypeReference<ExecuteT24Output<MsLoanCreateOutput>>() {});
            mappingErrorCode(output);
            return output;
        } catch (Exception e) {
            log.error("[ApiMsLoan] Exception createLoan: ", e);
            return null;
        }
    }


}

