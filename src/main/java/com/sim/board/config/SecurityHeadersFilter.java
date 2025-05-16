package com.sim.board.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // XSS 방어
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // 클릭재킹 방어
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // MIME 타입 스니핑 방어
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // HSTS (HTTPS 강제)
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

        // 참조자 정책
        httpResponse.setHeader("Referrer-Policy", "same-origin");

        // 기능 정책 - 위험한 기능 제한
        httpResponse.setHeader("Feature-Policy",
                "camera 'none'; microphone 'none'; geolocation 'none'; payment 'none'");

        // Content Security Policy - 더 강화된 정책
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com 'unsafe-inline'; " +
                        "style-src 'self' https://cdn.jsdelivr.net https://fonts.googleapis.com https://cdnjs.cloudflare.com 'unsafe-inline'; " +
                        "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
                        "img-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'; " +
                        "base-uri 'self'; " +
                        "object-src 'none'");

        // 캐시 제어 - 민감한 정보를 브라우저에 캐싱하지 않도록
        httpResponse.setHeader("Cache-Control", "no-store, max-age=0, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setHeader("Expires", "0");

        chain.doFilter(request, response);
    }
}