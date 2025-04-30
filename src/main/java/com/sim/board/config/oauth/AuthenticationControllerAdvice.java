package com.sim.board.config.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class AuthenticationControllerAdvice {

    private final AuthenticationSessionService authSessionService;

    // 모든 컨트롤러 메서드가 실행되기 전에 호출되어 필요한 세션 데이터를 모델에 추가
    @ModelAttribute
    public void addAuthProviderToModel(HttpServletRequest request) {
        // 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            HttpSession session = request.getSession(false);

            if (session != null) {
                // URL 파라미터로 provider가 전달된 경우 세션에 저장
                String providerParam = request.getParameter("provider");
                if (providerParam != null && !providerParam.isEmpty()) {
                    session.setAttribute("auth_provider", providerParam);
                }

                // 현재 인증 정보에서 OAuth 제공자 타입 확인
                authSessionService.saveProviderToSession(session, authentication);
            }
        }
    }
}