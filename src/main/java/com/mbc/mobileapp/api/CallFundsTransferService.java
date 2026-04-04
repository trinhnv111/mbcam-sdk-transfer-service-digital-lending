package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.transfer.ciftp.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@Component
public class CallFundsTransferService extends CallMicroService {

    public ExecuteT24Output<List<CiftpBankInfo>> getListBankCiftp(String provider, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);

        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.funds.transfer.host"));
        url.append(getUrl("microservice.funds.transfer.bank.list.url"));

        String urlFomat = String.format(url.toString(), provider);

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(urlFomat.toString());

        ExecuteT24Output<List<CiftpBankInfo>> output = getForMicroService(uri.build().toUri(), headers,
                new ParameterizedTypeReference<ExecuteT24Output<List<CiftpBankInfo>>>() {
                });
        mappingErrorCode(output);
        return output;

    }

    public ExecuteT24Output<CiftpAccountInquiryOutput> getInfoAccountToCasa(CiftpAccountInQuiryInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        headers.add("transactionId", messageId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.funds.transfer.host"));
        url.append(getUrl("microservice.funds.transfer.account.inquiry.url"));

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url.toString());

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("accountNumber", input.getAccountNumber());
        map.add("service", input.getService().name());
        map.add("transferType", input.getTransferType());
        map.add("participantCode", input.getParticipantCode());
        uri.queryParams(map);

        ExecuteT24Output<CiftpAccountInquiryOutput> output = getForMicroService(uri.build().toUri(), headers,
                new ParameterizedTypeReference<ExecuteT24Output<CiftpAccountInquiryOutput>>() {
                });
        mappingErrorCode(output);
        return output;

    }

    public ExecuteT24Output<CiftpChargeOutput> ciftpGwChargeTransfer(String provider, CiftpChargeInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        headers.add("transactionId", messageId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.funds.transfer.host"));
        url.append(getUrl("microservice.funds.transfer.charge.transfer.url"));

        String urlFormat = String.format(url.toString(), provider);
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(urlFormat);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("amount", input.getAmount());
        map.add("channel", input.getChannel());
        map.add("currency", input.getCurrency());
        map.add("flow", input.getFlow());
        if(Objects.nonNull(input.getPaymentTypeCode())) {
            map.add("paymentTypeCode", input.getPaymentTypeCode());
        }

        map.add("qrPayment", input.getQrPayment());
        map.add("service", input.getService().name());
        uri.queryParams(map);

        ExecuteT24Output<CiftpChargeOutput> output = getForMicroService(uri.build().toUri(), headers,
                new ParameterizedTypeReference<ExecuteT24Output<CiftpChargeOutput>>() {
                });
        mappingErrorCode(output);
        return output;
    }

    public ExecuteT24Output<LimitTransOutput> getLimitTrans(String provider, LimitTransInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        headers.add("transactionId", messageId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.funds.transfer.host"));
        url.append(getUrl("microservice.funds.transfer.limit.url"));

        String urlFormat = String.format(url.toString(), provider);
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(urlFormat);

        ExecuteT24Output<LimitTransOutput> output = postForMicroService(uri.toUriString(), headers, input,
                new ParameterizedTypeReference<ExecuteT24Output<LimitTransOutput>>() {
                });
        mappingErrorCode(output);
        return output;
    }

    public ExecuteT24Output<List<CiftpMakeConfigInfo>> makeTransferConfig(CiftpMakeConfig input, String custId, String requestId) {

        String messageId = Utility.getUUID();

        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.funds.transfer.host"));
        url.append(getUrl("microservice.funds.transfer.make.config.url"));

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url.toString());

        ExecuteT24Output<List<CiftpMakeConfigInfo>> output = postForMicroService(uri.toUriString(), headers, input,
                new ParameterizedTypeReference<ExecuteT24Output<List<CiftpMakeConfigInfo>>>() {
                });
        mappingErrorCode(output);
        return output;
    }



    public ExecuteT24Output<CiftpAccountInquiryOutput> getInfoAccountToWallet(CiftpAccountInQuiryInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        headers.add("transactionId", messageId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.funds.transfer.host"));
        url.append(getUrl("microservice.funds.transfer.account.inquiry.url"));

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url.toString());

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("accountNumber", input.getAccountNumber());
        map.add("service", input.getService().name());
        map.add("transferType", input.getTransferType());
        map.add("subType", input.getSubType());
        uri.queryParams(map);

        ExecuteT24Output<CiftpAccountInquiryOutput> output = getForMicroService(uri.build().toUri(), headers,
                new ParameterizedTypeReference<ExecuteT24Output<CiftpAccountInquiryOutput>>() {
                });
        mappingErrorCode(output);
        return output;

    }
}
