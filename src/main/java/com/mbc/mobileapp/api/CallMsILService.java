package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.dto.AccountVipDTO;
import com.mbc.common.il.base.ExecuteT24Input;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.il.base.ExecuteT24VersionInput;
import com.mbc.common.util.T24RoutineUtil;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.register.NonSavingAccount;
import com.mbc.mobileapp.api.model.register.NonSavingAcctDataOutput;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.api.model.saving.account.SavingAccountListInput;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Component
public class CallMsILService extends CallMicroService {
    
    private static String IL_HOST_URL;
    
    @PostConstruct
    public void init() {
        IL_HOST_URL = getUrl("il.rest.url");
    }
    
//    public ExecuteT24Output<List<AccountVipDTO>> getListVipAccount(ListAccountVipInput message, String custId, String requestId) throws IOException {
//
//
//        HttpHeaders header = buildHeader(custId, requestId, Utility.getUUID());
//
//        ExecuteT24Input input = new ExecuteT24Input();
//        input.setDestination(T24RoutineUtil.GET_ACCOUNT_NICE);
//        input.setVersion("1.0");
//        input.setMessage(message);
//
//        StringBuilder url = new StringBuilder();
//        url.append(IL_HOST_URL);
//        url.append(getUrl("il.t24.routine.url"));
//
//        ExecuteT24Output<List<AccountVipDTO>> output = postForMicroService(url.toString(), header, input,
//            new ParameterizedTypeReference<ExecuteT24Output<List<AccountVipDTO>>>() {
//            });
//        mappingErrorCode(output);
//        return output;
//    }
//
//    public ExecuteT24Output<List<AccountSaving>> getSavingAccountList(GetSavingAccountListInput message, String custId, String requestId) throws IOException {
//
//
//        HttpHeaders header = buildHeader(custId, requestId, Utility.getUUID());
//
//        ExecuteT24Input input = new ExecuteT24Input();
//        input.setDestination(T24RoutineUtil.GET_SAVING_ACCOUNT_LIST);
//        input.setVersion("1.0");
//        input.setMessage(message);
//
//        StringBuilder url = new StringBuilder();
//        url.append(IL_HOST_URL);
//        url.append(getUrl("il.t24.routine.url"));
//
//        ExecuteT24Output<List<AccountSaving>> output = postForMicroService(url.toString(), header, input,
//            new ParameterizedTypeReference<ExecuteT24Output<List<AccountSaving>>>() {
//            });
//        mappingErrorCode(output);
//        return output;
//    }
    
    public ExecuteT24Output<List<AccountSaving>> getSavingAccountListV3(SavingAccountListInput message, String custId, String requestId) throws IOException {
        
        
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



    public ExecuteT24Output<NonSavingAcctDataOutput> createNonSavingAccount(NonSavingAccount body, String custId,
                                                                            String requestId) {
        HttpHeaders header = buildHeader(custId, requestId, Utility.getUUID());
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.account.host"));
        url.append(getUrl("microservice.create.nonsavingaccount.url"));

        ExecuteT24Output<NonSavingAcctDataOutput> output = postForMicroService(url.toString(), header, body,
            new ParameterizedTypeReference<ExecuteT24Output<NonSavingAcctDataOutput>>() {
            });
        mappingErrorCode(output);

        return output;
    }
//
//    public ExecuteT24Output<OpenOnlineSavingOutput> createSavingAccount(OpenOnlineSavingInput body, String custId,
//            String requestId) {
//            HttpHeaders header = buildHeader(custId, requestId, Utility.getUUID());
//            StringBuilder url = new StringBuilder();
//            url.append(getUrl("microservice.host.url"));
//            url.append(":");
//            url.append(getUrl("microservice.saving.port"));
//            url.append(getUrl("microservice.saving.create.url"));
//
//            ExecuteT24Output<OpenOnlineSavingOutput> output = postForMicroService(url.toString(), header, body,
//                new ParameterizedTypeReference<ExecuteT24Output<OpenOnlineSavingOutput>>() {
//                });
//            mappingErrorCode(output);
//
//            return output;
//        }
//
//    public ExecuteT24Output<VipAccountInfo> createVipAccount(CreateVipAccountInput body, String custId,
//        String requestId) {
//        HttpHeaders header = buildHeader(custId, requestId, Utility.getUUID());
//        StringBuilder url = new StringBuilder();
//        url.append(getUrl("microservice.host.url"));
//        url.append(":");
//        url.append(getUrl("microservice.account.port"));
//        url.append(getUrl("microservice.create.vipaccount.url"));
//
//        ExecuteT24Output<VipAccountInfo> output = postForMicroService(url.toString(), header, body,
//            new ParameterizedTypeReference<ExecuteT24Output<VipAccountInfo>>() {
//            });
//        mappingErrorCode(output);
//
//        return output;
//    }
//
//    public ExecuteT24Output<CollectFeeVipAccountOutput> collectFeesVipAccount(CollectFeeVipAccount body, String custId,
//        String requestId, String transactionId) {
//        String clientMsgId = Utility.getUUID();
//        HttpHeaders header = buildHeader(custId, requestId, clientMsgId);
//        header.add("transactionId", transactionId);
//
//        StringBuilder url = new StringBuilder();
//        url.append(getUrl("il.rest.url"));
//        url.append(getUrl("il.t24.version.url"));
//
//        ExecuteT24VersionInput input = new ExecuteT24VersionInput();
//        input.setDestination(T24RoutineUtil.COLLECT_FEE_VIP_ACCOUNT);
//        input.setVersion("1.0");
//        input.setMessage(body);
//        input.setAction(T24RoutineUtil.T24_ACTION_AUTO_AUTH);
//        input.setBranchCode(ServiceConstant.BRANCH_CODE_HO);
//
//        ExecuteT24Output<CollectFeeVipAccountOutput> output = postForMicroService(url.toString(), header, input,
//            new ParameterizedTypeReference<ExecuteT24Output<CollectFeeVipAccountOutput>>() {
//            });
//        mappingErrorCode(output);
//
//        return output;
//    }
//
//    public ExecuteT24Output<CollectFeeVipAccountOutput> revertCollectFeesVipAccount(String ft, String custId,
//        String requestId) {
//        String messageId = Utility.getUUID();
//        HttpHeaders header = buildHeader(custId, requestId, messageId);
//        header.add("recordId", ft);
//        header.add("transactionId", messageId);
//        StringBuilder url = new StringBuilder();
//        url.append(getUrl("il.rest.url"));
//        url.append(getUrl("il.t24.version.url"));
//
//        ExecuteT24VersionInput input = new ExecuteT24VersionInput();
//        input.setDestination(T24RoutineUtil.COLLECT_FEE_VIP_ACCOUNT);
//        input.setVersion("1.0");
//        input.setMessage(null);
//        input.setAction(T24RoutineUtil.T24_ACTION_REVERT);
//        input.setBranchCode(ServiceConstant.BRANCH_CODE_HO);
//
//        ExecuteT24Output<CollectFeeVipAccountOutput> output = postForMicroService(url.toString(), header, input,
//            new ParameterizedTypeReference<ExecuteT24Output<CollectFeeVipAccountOutput>>() {
//            });
//        mappingErrorCode(output);
//
//        return output;
//    }
}
