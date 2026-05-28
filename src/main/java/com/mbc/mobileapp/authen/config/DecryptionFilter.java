package com.mbc.mobileapp.authen.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.util.JSON;
import com.mbc.common.util.Utility;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.authen.config.wrapper.DecryptedRequestWrapper;
import com.mbc.mobileapp.config.ErrorCode;
import com.mbc.mobileapp.exception.BusinessException;
import com.mbc.mobileapp.rest.bean.RestResponse;
import com.mbc.mobileapp.utils.TransmissionUtils;
import com.mbc.mobileapp.utils.validator.ValidatorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.encrypt", value = "enable", havingValue = "true", matchIfMissing = false)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DecryptionFilter extends OncePerRequestFilter {

    @Value("${app.encrypt.key}")
    protected String aeadKey;

    @Value("${server.servlet.context-path}")
    private String selfPath;

    private final TransmissionUtils transmissionUtils;
    protected final ObjectMapper mapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String bodyEncrypt = request.getReader().lines().collect(Collectors.joining());
            if (StringUtils.isEmpty(bodyEncrypt)) {
                throw new BusinessException(ErrorCode.MESSAGE_FORMAT_ERROR);
            }

            EncryptedRequest payload = JSON.parseObject(bodyEncrypt, EncryptedRequest.class);

            String resultValidate = ValidatorUtils.validateBean(payload);
            if (StringUtils.isNotEmpty(resultValidate))
                throw new BusinessException(resultValidate, ErrorCode.MESSAGE_FORMAT_ERROR);

            String bodyDecrypt = transmissionUtils.decrypt(aeadKey, payload.getCipherText(), payload.getNonce(), payload.getAad());

            DecryptedRequestWrapper decryptedRequest = new DecryptedRequestWrapper(request, bodyDecrypt);
            filterChain.doFilter(decryptedRequest, response);
        } catch (Exception e) {
            log.info("Failed to decrypt request: {}", e.getMessage());
            RestResponse resp = mappingResponseException(e);
            String responseBodyAsString = mapper.writeValueAsString(resp);
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            response.getOutputStream().write(responseBodyAsString.getBytes());
        }
    }

    private static RestResponse mappingResponseException(Exception e) {
        RestResponse resp = new RestResponse();
        com.mbc.common.validator.base.Validator.Result result = null;

        if (e instanceof BusinessException) {
            String messageError = Optional.ofNullable(e.getMessage())
                    .orElse(((BusinessException) e).getErrorCode().getSoaErrorDesc());
            result = new SimpleResult(messageError, false, ((BusinessException) e).getErrorCode().getSoaErrorCode());
            resp.setResult(result);
        } else {
            String messageError = Optional.ofNullable(e.getMessage())
                    .orElse(ErrorCode.INPUT_ENCRYPT_INVALID.getSoaErrorDesc());
            result = new SimpleResult(messageError, false, ErrorCode.INPUT_ENCRYPT_INVALID.getSoaErrorCode());
            resp.setResult(result);
        }
        resp.setRefNo(Utility.getUUID());
        log.error("[Decrypt Data Request Ex] {}", JSON.stringify(resp));
        return resp;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith(selfPath);
    }
}
