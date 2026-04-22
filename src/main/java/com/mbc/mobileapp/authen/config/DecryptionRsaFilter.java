package com.mbc.mobileapp.authen.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.util.JSON;
import com.mbc.common.util.TokenUtil;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.authen.config.wrapper.DecryptedRequestWrapper;
import com.mbc.mobileapp.config.ErrorCode;
import com.mbc.mobileapp.exception.BusinessException;
import com.mbc.mobileapp.rest.bean.RestResponse;
import com.mbc.mobileapp.utils.JoseUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.encrypt-body", value = "enable", havingValue = "true")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DecryptionRsaFilter extends OncePerRequestFilter {

    private TokenUtil tokenUtil;
    protected final ObjectMapper mapper;

    public DecryptionRsaFilter(ObjectMapper mapper) {
        this.tokenUtil = new TokenUtil();
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String bodyEncrypt = request.getReader().lines().collect(Collectors.joining());

        try {
            if (StringUtils.isEmpty(bodyEncrypt)) {
                throw new BusinessException(ErrorCode.MESSAGE_FORMAT_ERROR);
            }
            RSAPrivateKey privateKey = tokenUtil.getPrivateKey("rsakey/dev/private_key.pem");
            String bodyDecrypt = JoseUtils.parseJwe(bodyEncrypt, privateKey);

//            String bodyDecrypt = tokenUtil.decrypt(privateKey, bodyEncrypt.getBytes(StandardCharsets.UTF_8));

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
        Validator.Result result = null;

//        result = new SimpleResult(HttpStatus.BAD_REQUEST.getReasonPhrase(), false,
//                String.valueOf(HttpStatus.BAD_REQUEST.value()));
//        resp.setResult(result);

        if (e instanceof BusinessException) {
            result = new SimpleResult(((BusinessException) e).getErrorCode().getSoaErrorDesc(), false,
                    ((BusinessException) e).getErrorCode().getSoaErrorCode());
            resp.setResult(result);
        } else {
            result = new SimpleResult(ErrorCode.INPUT_ENCRYPT_INVALID.getSoaErrorDesc(), false,
                    ErrorCode.INPUT_ENCRYPT_INVALID.getSoaErrorCode());
            resp.setResult(result);
        }
        resp.setRefNo(Utility.getUUID());
        log.error("[Decrypt Data Request Ex] {}", JSON.stringify(resp));
        return resp;
    }

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getRequestURI();
//        return !path.startsWith("/mbc-sdk/") || !HttpMethod.POST.name().equals(request.getMethod());
//    }
}