package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroService;
import com.mbc.common.microservice.base.TokenBean;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Utility;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;

/**
 * Access token cho CBC portal integrate (OAuth2 client_credentials).
 */
@Component
@Getter
@Setter
public class CbcPortalToken extends CallMicroService {

    private TokenBean tokenBean;

    @PostConstruct
    public void postConstruct() {
        if (tokenBean == null) {
            refreshToken(null, Utility.getUUID());
        }
    }

    /**
     * Trả về token còn hạn sẵn trong cache;
     * nếu hết hạn hoặc chưa có → gọi lại OAuth2 server.
     */
    public synchronized TokenBean getTokenBean(String custId, String requestId) {
        if (tokenBean != null
                && tokenBean.getAccessToken() != null
                && !isExpired(tokenBean)) {
            return tokenBean;
        }
        refreshToken(custId, requestId);
        return tokenBean;
    }

    public void refreshToken(String custId, String requestId) {
        try {
            String clientMessageId = Utility.getUUID();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            // CBC spec: Authorization: Basic base64(username:password)
            headers.setBasicAuth(
                    env.getProperty("cbc.portal.client.id"),
                    env.getProperty("cbc.portal.client.secret")
            );
            headers.add("clientMessageId", clientMessageId);

            // CBC spec: grant_type=client_credentials trong form body
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

            String urlToken = env.getProperty("cbc.portal.auth.url");
            TokenBean newToken = restTemplate.postForObject(urlToken, entity, TokenBean.class);

            if (newToken != null && newToken.getAccessToken() != null) {
                // Tính thời điểm hết hạn; trừ 30s safety window
                long expiresInSec = 300L;
                try {
                    if (newToken.getExpiresIn() != null) {
                        expiresInSec = Long.parseLong(newToken.getExpiresIn());
                    }
                } catch (NumberFormatException ignore) { }
                newToken.setExpiresMilisecond(
                        System.currentTimeMillis() + Math.max(0, expiresInSec - 30) * 1000L
                );
            }

            tokenBean = newToken;
            AppLog.info("[CbcPortalToken] Token refreshed - requestId:" + requestId);
        } catch (Exception e) {
            AppLog.error("[CbcPortalToken] Failed to refresh token - requestId:" + requestId + " err:" + e.getMessage());
        }
    }

    private boolean isExpired(TokenBean bean) {
        if (bean.getExpiresMilisecond() <= 0) return true;
        return bean.getExpiresMilisecond() <= System.currentTimeMillis();
    }
}
