package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.digitalloan.output.MsLoanGetPdOutput;
import com.mbc.mobileapp.api.model.digitalloan.output.PaymentHistoryOutPut;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class ApiMsLoan extends CallMicroService {

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


}

