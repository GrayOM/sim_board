// src/main/java/com/sim/board/config/RateLimitInterceptor.java
package com.sim.board.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 인증된 사용자 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String key;

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            key = authentication.getName();
        } else {
            key = getClientIP(request);
        }

        // 요청 레이트 리밋 확인
        if (rateLimitService.isRequestLimited(key)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("너무 많은 요청을 보냈습니다. 잠시 후 다시 시도해주세요.");
            return false;
        }

        // 게시글 작성 요청인 경우 게시글 레이트 리밋 확인
        if (request.getMethod().equals("POST") && request.getRequestURI().endsWith("/boards")) {
            if (rateLimitService.isPostLimited(key)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("게시글 작성 횟수를 초과했습니다. 1시간 후 다시 시도해주세요.");
                return false;
            }
        }

        // 파일 업로드 요청인 경우 파일 업로드 레이트 리밋 확인
        if (request.getContentType() != null &&
                request.getContentType().startsWith("multipart/form-data")) {
            if (rateLimitService.isFileUploadLimited(key)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("파일 업로드 횟수를 초과했습니다. 1시간 후 다시 시도해주세요.");
                return false;
            }
        }

        return true;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}