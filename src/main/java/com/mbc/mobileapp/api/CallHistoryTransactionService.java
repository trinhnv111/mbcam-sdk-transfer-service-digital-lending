/* ---------------------------------------------------------------------------
 *  All Rights Reserved. Copyright(C) MB Bank, Ltd.
 *  revision : 3:42:25 PM 
 *  vendor   : MB Bank, Ltd.
 *  author   : Le Van Dan OS
 *  since    : 2021-11-22 3:42:25 PM 
 *  tagId    : mbcam-mobileapp-transfer
 * ---------------------------------------------------------------------------
 *  revision marking
 * --------------------------------------------------------------------------- */

package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.util.DateUtil;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.account.transaction.AcctTransactionHistory;
import com.mbc.mobileapp.api.model.account.transaction.history.TransactionHistoryDetailInput;
import com.mbc.mobileapp.api.model.account.transaction.history.TransactionHistoryInput;
import com.mbc.mobileapp.api.model.account.transaction.history.TransactionHistoryReceiverInput;
import com.mbc.mobileapp.api.model.account.transaction.history.bakong.GetDetailTransactionHistoryInput;
import com.mbc.mobileapp.api.model.account.transaction.history.bakong.GetDetailTransactionHistoryOutput;
import com.mbc.mobileapp.rest.account.history.TotalTransactionHistory;
import com.mbc.mobileapp.rest.account.history.TransHistoryInfo;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;


@Component
public class CallHistoryTransactionService extends CallMicroService {

    public ExecuteT24Output<List<AcctTransactionHistory>> getTransactionsTodayByAcc(TransactionHistoryInput input,
                                                                                    String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        headers.add("clientMessageId", messageId);
        headers.add("transactionId", messageId);

        StringBuilder url = new StringBuilder();
        url.append(getUrl("il.rest.url"));
        url.append(getUrl("il.t24.routine.url"));

        ExecuteT24Output<List<AcctTransactionHistory>> output = postForMicroService(url.toString(), headers, input,
            new ParameterizedTypeReference<ExecuteT24Output<List<AcctTransactionHistory>>>() {
            });
        mappingErrorCode(output);
        return output;

    }
    public ExecuteT24Output<List<AcctTransactionHistory>> getTransactionsDetail(TransactionHistoryInput input,
                                                                                    String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        headers.add("clientMessageId", messageId);
        headers.add("transactionId", messageId);

        StringBuilder url = new StringBuilder();
        url.append(getUrl("il.rest.url"));
        url.append(getUrl("il.t24.routine.url"));

        ExecuteT24Output<List<AcctTransactionHistory>> output = postForMicroService(url.toString(), headers, input,
                new ParameterizedTypeReference<ExecuteT24Output<List<AcctTransactionHistory>>>() {
                });
        mappingErrorCode(output);
        return output;

    }
    //Lich Su Giao Dich CHI TIET
    public ExecuteT24Output<List<TransHistoryInfo>> getTransactionInfo(TransactionHistoryDetailInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getUrl("microservice.trans.history.host"));
        urlBuilder.append(getUrl("microservice.trans.history.url"));
        
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(urlBuilder.toString());
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("endPostingDate",input.getEndPostingDate() != null ? DateFormatUtils.format(input.getEndPostingDate(), DateUtil.DATE_TIME_SIMPLE_REVERSE_2) : "");
        map.add("endTransactionDate", DateFormatUtils.format(input.getEndTransactionDate(), DateUtil.DATE_TIME_SIMPLE_REVERSE_2));
        map.add("startPostingDate", input.getStartPostingDate() != null ? DateFormatUtils.format(input.getStartPostingDate(), DateUtil.DATE_TIME_SIMPLE_REVERSE_2) : "");
        map.add("startTransactionDate", DateFormatUtils.format(input.getStartTransactionDate(), DateUtil.DATE_TIME_SIMPLE_REVERSE_2));
        map.add("sizeResponse", String.valueOf(input.getSizeResponse()));
        map.add("search", input.getSearch());  
        uri.queryParams(map);
       
        ExecuteT24Output<List<TransHistoryInfo>> output = getForMicroService(uri.build().toUri(), headers,
                new ParameterizedTypeReference<ExecuteT24Output<List<TransHistoryInfo>>>() {
                });
        mappingErrorCode(output);
        return output;

    }
    //Tong so Giao dich
    public ExecuteT24Output<TotalTransactionHistory> getTotalTransactionInfo(TransactionHistoryDetailInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getUrl("microservice.trans.history.host"));
        urlBuilder.append(getUrl("microservice.trans.history.url"));
        
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(urlBuilder.toString());
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("endPostingDate",input.getEndPostingDate() != null ? DateFormatUtils.format(input.getEndPostingDate(), DateUtil.DATE_TIME_SIMPLE_REVERSE_2) : "");
        map.add("endTransactionDate", DateFormatUtils.format(input.getEndTransactionDate(), DateUtil.DATE_TIME_SIMPLE_REVERSE_2));
        map.add("startPostingDate", input.getStartPostingDate() != null ? DateFormatUtils.format(input.getStartPostingDate(), DateUtil.DATE_TIME_SIMPLE_REVERSE_2) : "");
        map.add("startTransactionDate", DateFormatUtils.format(input.getStartTransactionDate(), DateUtil.DATE_TIME_SIMPLE_REVERSE_2));
        map.add("sizeResponse", String.valueOf(input.getSizeResponse()));
        map.add("search", input.getSearch());  
        uri.queryParams(map);

        ExecuteT24Output<TotalTransactionHistory> output = getForMicroService(uri.build().toUri(), headers,
                new ParameterizedTypeReference<ExecuteT24Output<TotalTransactionHistory>>() {
                });
        mappingErrorCode(output);
        return output;

    }


    public ExecuteT24Output<List<TransHistoryInfo>> getHistoryReceiver(TransactionHistoryReceiverInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getUrl("microservice.trans.history.host"));
        urlBuilder.append(getUrl("microservice.trans.history.receiver.url"));

        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(urlBuilder.toString());

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("endTransactionDate", DateFormatUtils.format(input.getEndTransactionDate(), DateUtil.DATE_WITH_DASH_REVERSE)+" 23:59:59");
        map.add("oriTerminalId", input.getOriTerminalId());
        map.add("startTransactionDate", DateFormatUtils.format(input.getStartTransactionDate(), DateUtil.DATE_WITH_DASH_REVERSE)+" 00:00:00");
        uri.queryParams(map);

        ExecuteT24Output<List<TransHistoryInfo>> output = getForMicroService(uri.build().toUri(), headers,
                new ParameterizedTypeReference<ExecuteT24Output<List<TransHistoryInfo>>>() {
                });
        mappingErrorCode(output);
        return output;

    }

    public ExecuteT24Output<GetDetailTransactionHistoryOutput> getCiftpDetailHistoryTransaction(GetDetailTransactionHistoryInput input, String custId, String requestId) {
        String messageId = Utility.getUUID();
        HttpHeaders headers = buildHeader(custId, requestId, messageId);
        headers.add("transactionId", messageId);
        StringBuilder url = new StringBuilder();
        url.append(getUrl("microservice.ciftpgw.host"));
        url.append(getUrl("microservice.ciftpgw.transaction.history.url"));

        String formatUrl = url.toString();
        UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(formatUrl);

        ExecuteT24Output<GetDetailTransactionHistoryOutput> output = postForMicroService(uri.toUriString(), headers, input,
                new ParameterizedTypeReference<ExecuteT24Output<GetDetailTransactionHistoryOutput>>() {
                });
        mappingErrorCode(output);
        return output;

    }
}
