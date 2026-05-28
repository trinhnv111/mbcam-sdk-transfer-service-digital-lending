package com.mbc.mobileapp.authen.config.wrapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;

public class DecryptedRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;
    private final String contentType;

    public DecryptedRequestWrapper(HttpServletRequest request, String decryptedJson) {
        this(request, decryptedJson, MediaType.APPLICATION_JSON_VALUE);
    }

    public DecryptedRequestWrapper(HttpServletRequest request, String body, String contentType) {
        super(request);
        this.body = body.getBytes();
        this.contentType = contentType;
    }

    @Override
    public String getHeader(String name) {
        if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
            return contentType;
        }
        return super.getHeader(name);
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            public boolean isFinished() {
                return bais.available() == 0;
            }

            public boolean isReady() {
                return true;
            }

            public void setReadListener(ReadListener listener) {
            }

            public int read() {
                return bais.read();
            }
        };
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            return Collections.enumeration(Collections.singletonList(contentType));
        }
        return super.getHeaders(name);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

}
