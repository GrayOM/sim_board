// src/main/java/com/sim/board/config/SecurityHeadersFilter.java
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
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                        "style-src 'self' https://cdn.jsdelivr.net https://fonts.googleapis.com https://cdnjs.cloudflare.com; " +
                        "font-src 'self' https://fonts.gstatic.com https://cdnjs.cloudflare.com; " +
                        "img-src 'self' data:; " +
                        "connect-src 'self';");

        chain.doFilter(request, response);
    }
}