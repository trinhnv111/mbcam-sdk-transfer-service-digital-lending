package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Utility;
import com.mbc.mobileapp.api.model.cbc.input.GetCbcDataRequest;
import com.mbc.mobileapp.api.model.cbc.output.GetCbcDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

/**
 * HTTP client CBC portal integrate.
 * Pattern: extends CallMicroService — dùng restTemplate/env/buildHeader() từ cha.
 */
@Slf4j
@Service
public class ApiCbcPortal extends CallMicroService {

    @Autowired
    private CbcPortalToken cbcPortalToken;

    /**
     * Gọi POST /integrate/v1/get-cbc-data
     *
     * @param body          request body (requestBy, appCode, idNumber)
     * @param clientUserId  ID user thực hiện call (M theo spec)
     * @param requestId     tracing requestId
     * @return ResponseEntity<GetCbcDataResponse> hoặc null nếu timeout
     */
    public ResponseEntity<GetCbcDataResponse> getCbcData(GetCbcDataRequest body,
                                                         String clientUserId,
                                                         String requestId) {
        return doGetCbcData(body, clientUserId, requestId, false);
    }

    private ResponseEntity<GetCbcDataResponse> doGetCbcData(GetCbcDataRequest body,
                                                             String clientUserId,
                                                             String requestId,
                                                             boolean isRetry) {
        String clientMessageId = Utility.getUUID();
        String url = buildUrl();

        try {
            String accessToken = cbcPortalToken.getTokenBean(clientUserId, requestId).getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("clientMessageId", clientMessageId);
            headers.add("clientUserId", clientUserId != null ? clientUserId : "");
            headers.setBearerAuth(accessToken);

            HttpEntity<GetCbcDataRequest> entity = new HttpEntity<>(body, headers);

            log.info("[ApiCbcPortal] POST {} - requestId:{}, clientMessageId:{}, idNumber:{}",
                    url, requestId, clientMessageId, body != null ? body.getIdNumber() : null);

            ResponseEntity<GetCbcDataResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, GetCbcDataResponse.class);

            // 401 → refresh token và retry 1 lần (giống CallMicroService pattern)
            if (HttpStatus.UNAUTHORIZED.equals(response.getStatusCode()) && !isRetry) {
                log.warn("[ApiCbcPortal] 401 received, refreshing token and retry - requestId:{}", requestId);
                cbcPortalToken.refreshToken(clientUserId, requestId);
                return doGetCbcData(body, clientUserId, requestId, true);
            }

            log.info("[ApiCbcPortal] Response status:{} - requestId:{}", response.getStatusCode(), requestId);
            return response;

        } catch (ResourceAccessException e) {
            log.error("[ApiCbcPortal] HTTP TIMEOUT/CONN - url:{}, requestId:{}, msg:{}",
                    url, requestId, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("[ApiCbcPortal] Unexpected error - url:{}, requestId:{}", url, requestId, e);
            AppLog.error(e);
            return null;
        }
    }

    private String buildUrl() {
        String baseUrl = env.getProperty("cbc.portal.base-url");
        String path = env.getProperty("cbc.portal.get-cbc-data.path");
        return (baseUrl != null ? baseUrl : "") + (path != null ? path : "");
    }
}
