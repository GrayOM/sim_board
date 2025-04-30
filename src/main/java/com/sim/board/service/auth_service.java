package com.sim.board.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class auth_service {

    /**
     * 인증 객체로부터 사용자명을 추출합니다.
     * OAuth2 인증과 일반 인증 모두 처리합니다.
     */
    public String extractUsername(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        // OAuth2 로그인인 경우
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

            // 소셜 로그인 제공자별 사용자 정보 추출
            if ("google".equals(provider)) {
                return oauth2User.getAttribute("email");
            } else if ("kakao".equals(provider)) {
                Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                    return (String) kakaoAccount.get("email");
                } else {
                    return "kakao-user-" + oauth2User.getAttribute("id");
                }
            } else if ("naver".equals(provider)) {
                Map<String, Object> response = oauth2User.getAttribute("response");
                if (response != null && response.containsKey("email")) {
                    return (String) response.get("email");
                } else if (response != null && response.containsKey("id")) {
                    return "naver-user-" + response.get("id");
                }
            }
        }
        // 일반 로그인인 경우
        else if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }

        // 기타 경우
        return authentication.getName();
    }
}