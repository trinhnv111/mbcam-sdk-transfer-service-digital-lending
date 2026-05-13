package com.mbc.mobileapp.api;

import com.mbc.common.api.ApiBase;
import com.mbc.common.il.base.ErrorInfo;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.microservice.base.TokenBean;
import com.mbc.common.util.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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


    /**
     * Lấy CBC Keycloak token.
     * Dùng buildHeader() từ ApiBase để set đúng clientMessageId + info-log
     * cho RestApiClientLogInterceptor.
     */
    private synchronized String getCbcToken(boolean forceRefresh, String custId, String requestId) {
        if (cbcTokenBean != null && !forceRefresh) {
            return cbcTokenBean.getAccessToken();
        }
        try {
            String tokenMsgId = Utility.getUUID();
            // Dùng custId + requestId thật từ user session để interceptor log đúng context
            HttpHeaders headers = buildHeader(custId, requestId, tokenMsgId);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
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

    /**
     * Gọi API get-cbc-data v2 từ CBC Portal.
     *
     * @param clientMessageId - ID giao dịch sinh ra từ client
     * @param clientUserId    - ID user thực hiện call API
     * @param requestBy       - SDKeM
     * @param appCode         - Đầu kênh ( MOBILEAPP)
     * @param idNumbers       - List ID number của KH
     */
    public ExecuteT24Output<List<Map<String, Object>>> getCbcData(String clientMessageId, String clientUserId,
                                                            String requestBy, String appCode, List<String> idNumbers) {
        return callApi(clientMessageId, clientUserId, requestBy, appCode, idNumbers, false);
    }

    private ExecuteT24Output<List<Map<String, Object>>> callApi(String clientMessageId, String clientUserId,
                                                          String requestBy, String appCode,
                                                          List<String> idNumbers, boolean isRetry) {
        try {
            String custId = clientUserId != null ? clientUserId : "SYSTEM";
            String requestId = Utility.getUUID();
            String token = getCbcToken(isRetry, custId, requestId);
            if (token == null) return null;

            String messageId = (clientMessageId != null) ? clientMessageId : Utility.getUUID();

            HttpHeaders headers = buildHeader(custId, requestId, messageId);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("ClientUserId", clientUserId);

            Map<String, Object> body = new HashMap<>();
            body.put("requestBy", requestBy);
            body.put("appCode", appCode);
            body.put("idNumber", idNumbers);

            HttpEntity<Object> request = new HttpEntity<>(body, headers);

            ResponseEntity<ExecuteT24Output<List<Map<String, Object>>>> response =
                    restTemplate.exchange(
                            cbcApiUrl,
                            HttpMethod.POST,
                            request,
                            new ParameterizedTypeReference<ExecuteT24Output<List<Map<String, Object>>>>() {}
                    );

            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED && !isRetry) {
                log.info("[ApiCBC] Token expired, retrying...");
                return callApi(clientMessageId, clientUserId, requestBy, appCode, idNumbers, true);
            }

            ExecuteT24Output<List<Map<String, Object>>> output = response.getBody();
            if (output != null) mappingErrorCode(output);
            return output;

        } catch (HttpClientErrorException.Unauthorized e) {
            if (!isRetry) {
                log.info("[ApiCBC] Token expired (exception), retrying...");
                return callApi(clientMessageId, clientUserId, requestBy, appCode, idNumbers, true);
            }
            log.error("[ApiCBC] Unauthorized after retry: ", e);
        } catch (Exception e) {
            log.error("[ApiCBC] Exception calling getCbcData: ", e);
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