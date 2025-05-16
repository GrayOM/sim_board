// src/main/java/com/sim/board/config/XssRequestWrapper.java
package com.sim.board.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.util.*;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.STYLES)
            .and(Sanitizers.TABLES);

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return value == null ? null : sanitize(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] sanitizedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitizedValues[i] = sanitize(values[i]);
        }
        return sanitizedValues;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> paramMap = super.getParameterMap();
        Map<String, String[]> sanitizedMap = new HashMap<>();

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String[] values = entry.getValue();
            String[] sanitizedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitizedValues[i] = sanitize(values[i]);
            }
            sanitizedMap.put(entry.getKey(), sanitizedValues);
        }

        return Collections.unmodifiableMap(sanitizedMap);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return value == null ? null : sanitize(value);
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        // HTML 태그 샌드박싱
        return POLICY.sanitize(value);
    }
}