package com.mbc.mobileapp.api;

import com.mbc.common.api.ApiBase;
import com.mbc.common.il.base.ErrorInfo;
import com.mbc.common.microservice.base.TokenBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import com.mbc.common.il.base.ExecuteT24Output;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ApiCBC extends ApiBase {

    @Value("${api.cbc.url}")
    private String cbcApiUrl;

    @Value("${keyclock.cbc.client.id}")
    private String cbcClientId;

    @Value("${keyclock.cbc.client.secret}")
    private String cbcClientSecret;

    @Value("${keyclock.gettoken.url}")
    private String tokenUrl;

    private TokenBean cbcTokenBean;

    private synchronized String getCbcToken(boolean forceRefresh) {
        if (cbcTokenBean != null && !forceRefresh) {
            return cbcTokenBean.getAccessToken();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
            map.add("client_id", cbcClientId);
            map.add("client_secret", cbcClientSecret);
            map.add("grant_type", "client_credentials");
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            cbcTokenBean = restTemplate.postForObject(tokenUrl, entity, TokenBean.class);
            return cbcTokenBean.getAccessToken();
        } catch (Exception e) {
            log.error("[ApiCBC] Error getting CBC token", e);
            return null;
        }
    }

    public ExecuteT24Output<Map<String, Object>> getCbcData(String clientMessageId, String clientUserId, String requestBy, String appCode, List<String> idNumbers) {
        return callApi(clientMessageId, clientUserId, requestBy, appCode, idNumbers, false);
    }

    private ExecuteT24Output<Map<String, Object>> callApi(String clientMessageId, String clientUserId, String requestBy, String appCode, List<String> idNumbers, boolean isRetry) {
        try {
            String token = getCbcToken(isRetry);
            if (token == null) return null;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("clientMessageId", clientMessageId);
            headers.set("clientUserId", clientUserId);

            Map<String, Object> body = new HashMap<>();
            body.put("requestBy", requestBy);
            body.put("appCode", appCode);
            body.put("idNumber", idNumbers);

            URI uri = new URI(cbcApiUrl);
            ResponseEntity<ExecuteT24Output<Map<String, Object>>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<ExecuteT24Output<Map<String, Object>>>() {}
            );

            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && !isRetry) {
                log.info("[ApiCBC] Token expired, retrying...");
                return callApi(clientMessageId, clientUserId, requestBy, appCode, idNumbers, true);
            }

            ExecuteT24Output<Map<String, Object>> output = response.getBody();
            if (output != null) mappingErrorCode(output);
            return output;

        } catch (Exception e) {
            log.error("[ApiCBC] Exception calling getCbcData: ", e);
            if (e instanceof HttpClientErrorException.Unauthorized && !isRetry) {
                log.info("[ApiCBC] Token expired (exception), retrying...");
                return callApi(clientMessageId, clientUserId, requestBy, appCode, idNumbers, true);
            }
        }
        return null;
    }

    protected <T> void mappingErrorCode(ExecuteT24Output<T> output) {
        if (output == null) return;
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setErrorCode(output.getSoaErrorCode() != null && output.getSoaErrorCode().startsWith("MC")
                ? output.getSoaErrorCode().substring(2)
                : output.getSoaErrorCode());
        errorInfo.setErrorDesc(output.getSoaErrorDesc());
        errorInfo.setErrorDetail(output.getError());
        output.setErrorInfo(errorInfo);
    }
}
