package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.il.base.ExecuteT24Input;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.T24RoutineUtil;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.api.model.saving.account.detail.GetSavingAccountListInput;
import com.mbc.mobileapp.api.model.saving.close.DepositClosureInput;
import com.mbc.mobileapp.api.model.saving.close.DepositClosureOutput;
import com.mbc.mobileapp.api.model.saving.cob.CheckCoBInput;
import com.mbc.mobileapp.api.model.saving.cob.CheckCoBOutput;
import com.mbc.mobileapp.api.model.saving.interest.InterestInput;
import com.mbc.mobileapp.api.model.saving.interest.InterestOutput;
import com.mbc.mobileapp.api.model.saving.open.OpenSavingInput;
import com.mbc.mobileapp.api.model.saving.open.OpenSavingOutput;
import com.mbc.mobileapp.api.model.saving.topup.TopUpSavingDepositOutput;
import com.mbc.mobileapp.api.model.saving.topup.TopupSavingDepositInput;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Component
public class CallSavingService extends CallMicroService {

    public ExecuteT24Output<List<AccountSaving>> getSavingAccountList(GetSavingAccountListInput message, String custId, String requestId) throws IOException {


        HttpHeaders header = buildHeader(custId, requestId, Utility.getUUID());

        ExecuteT24Input input = new ExecuteT24Input();
        input.setDestination(T24RoutineUtil.GET_SAVING_ACCOUNT_LIST);
        input.setVersion("1.0");
        input.setMessage(message);

        StringBuilder url = new StringBuilder();
        url.append(IL_HOST_URL);
        url.append(getUrl("il.t24.routine.url"));

        ExecuteT24Output<List<AccountSaving>> output = postForMicroService(url.toString(), header, input,
                new ParameterizedTypeReference<ExecuteT24Output<List<AccountSaving>>>() {
                });
        mappingErrorCode(output);
        return output;
    }

    public ExecuteT24Output<List<AccountSaving>> getSavingAccountListV3(GetSavingAccountListInput message, String custId, String requestId) throws IOException {


        HttpHeaders header = buildHeader(custId, requestId, Utility.getUUID());

        ExecuteT24Input input = new ExecuteT24Input();
        input.setDestination(T24RoutineUtil.GET_SAVING_ACCOUNT_LIST_V3);
        input.setVersion("1.0");
        input.setMessage(message);

        StringBuilder url = new StringBuilder();
        url.append(IL_HOST_URL);
        url.append(getUrl("il.t24.routine.url"));

        ExecuteT24Output<List<AccountSaving>> output = postForMicroService(url.toString(), header, input,
                new ParameterizedTypeReference<ExecuteT24Output<List<AccountSaving>>>() {
                });
        mappingErrorCode(output);
        return output;
    }

    public ExecuteT24Output<InterestOutput> getInterestRate(InterestInput input, String custId,
                                                            String requestId) {
        HttpHeaders header = buildHeader(custId, requestId, Utility.getUUID());
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.saving.host"));
        url.append(getUrl("microservice.saving.fixed.deposit.get.interest.rate"));
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(url.toString());
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("resident", input.getResident());
        map.add("productCode",input.getProductCode());
        uri.queryParams(map);
        ExecuteT24Output<InterestOutput> output = getForMicroService(uri.build().toUri(), header,
                new ParameterizedTypeReference<ExecuteT24Output<InterestOutput>>() {
                });
        mappingErrorCode(output);

        return output;
    }

    public ExecuteT24Output<OpenSavingOutput> openSavingFixedDeposit(OpenSavingInput input, String custId,
                                                                     String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders header = buildHeader(custId, requestId, messageId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.saving.host"));
        url.append(getUrl("microservice.saving.fixed.deposit.open"));

        ExecuteT24Output<OpenSavingOutput> output = postForMicroService(url.toString(), header, input,
                new ParameterizedTypeReference<ExecuteT24Output<OpenSavingOutput>>() {
                });
        mappingErrorCode(output);

        return output;
    }

    public ExecuteT24Output<DepositClosureOutput> closeSavingFixedDeposit(DepositClosureInput input, String custId,
                                                                          String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders header = buildHeader(custId, requestId, messageId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.saving.host"));
        url.append(getUrl("microservice.saving.fixed.deposit.close"));

        ExecuteT24Output<DepositClosureOutput> output = postForMicroService(url.toString(), header, input,
                new ParameterizedTypeReference<ExecuteT24Output<DepositClosureOutput>>() {
                });
        mappingErrorCode(output);

        return output;
    }

    public ExecuteT24Output<CheckCoBOutput> checkCoB(CheckCoBInput input, String custId,
                                                     String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders header = buildHeader(custId, requestId, messageId);
        header.add("RecordId","COB");
        header.add("transactionId",requestId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("il.rest.url"));
        url.append(getUrl("il.t24.version.url"));

        ExecuteT24Output<CheckCoBOutput> output = postForMicroService(url.toString(), header, input,
                new ParameterizedTypeReference<ExecuteT24Output<CheckCoBOutput>>() {
                });
        mappingErrorCode(output);

        return output;
    }


    public ExecuteT24Output<TopUpSavingDepositOutput> topupSavingDeposit(TopupSavingDepositInput input, String custId,
                                                                          String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders header = buildHeader(custId, requestId, messageId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.saving.host"));
        url.append(getUrl("microservice.saving.fixed.deposit.topup"));

        ExecuteT24Output<TopUpSavingDepositOutput> output = postForMicroService(url.toString(), header, input,
                new ParameterizedTypeReference<ExecuteT24Output<TopUpSavingDepositOutput>>() {
                });
        mappingErrorCode(output);

        return output;
    }

}
