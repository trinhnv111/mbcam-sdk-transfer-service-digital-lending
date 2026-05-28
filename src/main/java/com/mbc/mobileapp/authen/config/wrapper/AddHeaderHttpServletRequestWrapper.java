package com.mbc.mobileapp.authen.config.wrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

public class AddHeaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, String> headerMap = new HashMap<>();

    public AddHeaderHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void addHeader(String name, String value) {
        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String headerValue = super.getHeader(name);
        if (headerMap.containsKey(name)) {
            headerValue = headerMap.get(name);
        }
        return headerValue;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> headerNames = Collections.list(super.getHeaderNames());
        headerNames.addAll(headerMap.keySet());
        return Collections.enumeration(headerNames);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> headerNames = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name)) {
            headerNames.add(headerMap.get(name));
        }
        return Collections.enumeration(headerNames);
    }
}
