package com.sim.board.config.oauth;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationSessionService {

    private static final String AUTH_PROVIDER_KEY = "auth_provider";

    // 인증 제공자 정보를 세션에 저장
    public void saveProviderToSession(HttpSession session, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            String provider = token.getAuthorizedClientRegistrationId();
            session.setAttribute(AUTH_PROVIDER_KEY, provider);
        }
    }

    // 세션에서 인증 제공자 정보 가져오기
    public String getProviderFromSession(HttpSession session) {
        return (String) session.getAttribute(AUTH_PROVIDER_KEY);
    }
}