// src/main/java/com/sim/board/service/SecurityAuditService.java
package com.sim.board.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
public class SecurityAuditService {

    private static final Logger logger = Logger.getLogger(SecurityAuditService.class.getName());

    // 보안 감사 로그 메소드
    public void logSecurityEvent(String eventType, String username, String details, HttpServletRequest request) {
        String clientIP = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        String logMessage = String.format(
                "[보안 이벤트] 유형: %s, 시간: %s, 사용자: %s, IP: %s, 사용자 에이전트: %s, 상세: %s",
                eventType,
                LocalDateTime.now(),
                username,
                clientIP,
                userAgent,
                details
        );

        logger.warning(logMessage);
    }

    // 로그인 성공 이벤트
    public void logLoginSuccess(String username, HttpServletRequest request) {
        logSecurityEvent("로그인 성공", username, "사용자 로그인 성공", request);
    }

    // 로그인 실패 이벤트
    public void logLoginFailure(String username, String reason, HttpServletRequest request) {
        logSecurityEvent("로그인 실패", username, "이유: " + reason, request);
    }

    // 계정 잠금 이벤트
    public void logAccountLocked(String username, HttpServletRequest request) {
        logSecurityEvent("계정 잠금", username, "로그인 시도 횟수 초과로 계정 잠금", request);
    }

    // 비밀번호 변경 이벤트
    public void logPasswordChange(String username, HttpServletRequest request) {
        logSecurityEvent("비밀번호 변경", username, "사용자 비밀번호 변경됨", request);
    }

    // 접근 거부 이벤트
    public void logAccessDenied(String username, String resource, HttpServletRequest request) {
        logSecurityEvent("접근 거부", username, "리소스: " + resource, request);
    }

    // 위험 파일 업로드 이벤트
    public void logMaliciousFileUpload(String username, String filename, HttpServletRequest request) {
        logSecurityEvent("위험 파일 업로드", username, "파일명: " + filename, request);
    }

    // 클라이언트 IP 가져오기
    private String getClientIP(HttpServlet