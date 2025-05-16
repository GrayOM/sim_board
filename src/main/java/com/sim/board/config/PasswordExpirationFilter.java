// src/main/java/com/sim/board/config/PasswordExpirationFilter.java
package com.sim.board.config;

import com.sim.board.domain.user;
import com.sim.board.service.user_service;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Component
public class PasswordExpirationFilter implements Filter {

    private final user_service userService;
    private static final int PASSWORD_EXPIRATION_DAYS = 90; // 비밀번호 만료 기간 (일)

    // 비밀번호 변경 페이지 및 정적 리소스는 필터에서 제외
    private static final List<String> EXCLUDED_URLS = Arrays.asList(
            "/login", "/logout", "/register", "/user/change-password",
            "/css/", "/js/", "/images/", "/error", "/api/"
    );

    public PasswordExpirationFilter(user_service userService) {
        this.userService = userService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 현재 요청 경로
        String requestURI = httpRequest.getRequestURI();

        // 제외 URL 확인
        if (isExcludedUrl(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 인증된 사용자 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            try {
                user user = userService.getUserByUsername(authentication.getName());

                // 소셜 로그인 사용자는 제외
                if (user.getProvider() != null && !user.getProvider().isEmpty()) {
                    chain.doFilter(request, response);
                    return;
                }

                // 비밀번호 변경 시간 확인
                LocalDateTime passwordChangedAt = user.getPasswordChangedAt();
                if (passwordChangedAt != null) {
                    LocalDateTime expirationDate = passwordChangedAt.plusDays(PASSWORD_EXPIRATION_DAYS);
                    if (LocalDateTime.now().isAfter(expirationDate)) {
                        // 비밀번호 만료됨 - 비밀번호 변경 페이지로 리다이렉트
                        httpResponse.sendRedirect("/user/change-password?expired=true");
                        return;
                    }
                }
            } catch (Exception e) {
                // 사용자 정보 조회 실패 시 계속 진행
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isExcludedUrl(String url) {
        return EXCLUDED_URLS.stream().anyMatch(url::startsWith);
    }
}