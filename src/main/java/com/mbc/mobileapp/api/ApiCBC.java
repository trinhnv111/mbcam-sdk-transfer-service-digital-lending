package com.mbc.mobileapp.api;

import com.mbc.common.api.CallMicroServiceTokenKeyClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import com.mbc.common.il.base.ExecuteT24Output;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ApiCBC extends CallMicroServiceTokenKeyClock {

    @Value("${api.cbc.url:http://localhost:8080/integrate/v1/get-cbc-data}")
    private String cbcApiUrl;

    public ExecuteT24Output<Map<String, Object>> getCbcData(String clientMessageId, String clientUserId, String requestBy, String appCode, List<String> idNumbers) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("clientMessageId", clientMessageId);
            headers.set("clientUserId", clientUserId);

            Map<String, Object> body = new HashMap<>();
            body.put("requestBy", requestBy);
            body.put("appCode", appCode);
            body.put("idNumber", idNumbers);

            URI uri = new URI(cbcApiUrl);
            ExecuteT24Output<Map<String, Object>> response = postForMicroService(uri.toString(), headers, body, new ParameterizedTypeReference<ExecuteT24Output<Map<String, Object>>>() {});
            mappingErrorCode(response);
            return response;
        } catch (Exception e) {
            log.error("[ApiCBC] Exception calling getCbcData: ", e);
        }
        return null;
    }
}
