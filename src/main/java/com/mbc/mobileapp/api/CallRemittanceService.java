package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.remittance.input.RemittanceAddressInput;
import com.mbc.mobileapp.api.model.remittance.input.RemittanceGetAccountNameInput;
import com.mbc.mobileapp.api.model.remittance.input.RemittanceMakeTransferFinishInput;
import com.mbc.mobileapp.api.model.remittance.input.init.RemittanceMakeTransferInitInput;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceAddressOutput;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceBankListOutput;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceMakeTransferFinishOutput;
import com.mbc.mobileapp.api.model.remittance.output.RemittanceMakeTransferInitOutput;
import com.mbc.mobileapp.rest.remittance.getaccountname.GetAccountName;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class CallRemittanceService extends CallMicroService {
    public ExecuteT24Output<List<RemittanceBankListOutput>> getBankList(String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);

        String formatUrl = getUrl("microservice.host.url") +
                ":" +
                getUrl("microservice.remittance.port") +
                getUrl("microservice.remittance.get.bank.list");

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(formatUrl);

        ExecuteT24Output<List<RemittanceBankListOutput>> output = getForMicroService(uri.build().toUri(), headers,
                new ParameterizedTypeReference<ExecuteT24Output<List<RemittanceBankListOutput>>>() {
                });
        mappingErrorCode(output);
        return output;

    }

    public ExecuteT24Output<GetAccountName> getAccountName(RemittanceGetAccountNameInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);

        String formatUrl = getUrl("microservice.host.url") +
                ":" +
                getUrl("microservice.remittance.port") +
                getUrl("microservice.remittance.get.account.name");

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(formatUrl);

        ExecuteT24Output<GetAccountName> output = postForMicroService(uri.toUriString(), headers, input,
                new ParameterizedTypeReference<ExecuteT24Output<GetAccountName>>() {
                });
        mappingErrorCode(output);
        return output;

    }

    public ExecuteT24Output<RemittanceMakeTransferInitOutput> init(RemittanceMakeTransferInitInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);

        String formatUrl = getUrl("microservice.host.url") +
                ":" +
                getUrl("microservice.remittance.port") +
                getUrl("microservice.remittance.make-transfer-init");

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(formatUrl);

        ExecuteT24Output<RemittanceMakeTransferInitOutput> output = postForMicroService(uri.toUriString(), headers, input,
                new ParameterizedTypeReference<ExecuteT24Output<RemittanceMakeTransferInitOutput>>() {
                });
        mappingErrorCode(output);
        return output;

    }


    public ExecuteT24Output<RemittanceMakeTransferFinishOutput> finish(RemittanceMakeTransferFinishInput input,String transId, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        headers.add("transactionId",transId);
        String formatUrl = getUrl("microservice.host.url") +
                ":" +
                getUrl("microservice.remittance.port") +
                getUrl("microservice.remittance.make-transfer-finish");

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(formatUrl);

        ExecuteT24Output<RemittanceMakeTransferFinishOutput> output = postForMicroService(uri.toUriString(), headers, input,
                new ParameterizedTypeReference<ExecuteT24Output<RemittanceMakeTransferFinishOutput>>() {
                });
        mappingErrorCode(output);
        return output;

    }
    
    public ExecuteT24Output<List<RemittanceAddressOutput>> getAdrressVn(RemittanceAddressInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
//        headers.add("transactionId",transId);
        String formatUrl = getUrl("microservice.host.url") +
                ":" +
                getUrl("microservice.remittance.port") +
                getUrl("microservice.remittance.address.vn");

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(formatUrl);

        ExecuteT24Output<List<RemittanceAddressOutput>> output = postForMicroService(uri.toUriString(), headers, input,
                new ParameterizedTypeReference<ExecuteT24Output<List<RemittanceAddressOutput>>>() {
                });
        mappingErrorCode(output);
        return output;

    }
}
