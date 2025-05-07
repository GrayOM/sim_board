package com.sim.board.config.oauth;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class User_session_manager {

    private static final String AUTH_PROVIDER_KEY = "auth_provider";
    private static final String AUTH_USERNAME_KEY = "auth_username";

    // 인증 제공자 정보를 세션에 저장
    public void saveProviderToSession(HttpSession session, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) { //OAuth2 로 로그인했을경우에
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            String provider = token.getAuthorizedClientRegistrationId(); // 제공자 ID 가져오기
            session.setAttribute(AUTH_PROVIDER_KEY, provider); // 세션에 인증된 사용자 정보 저장
            // OAuth2 로그인 사용자의 이메일/사용자명도 세션에 저장
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String username = extractOAuth2Username(oauth2User, provider);
            session.setAttribute(AUTH_USERNAME_KEY, username);
        }
        else if (authentication.getPrincipal() instanceof UserDetails) {
            // 일반 로그인의 경우 사용자명만 저장
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            session.setAttribute(AUTH_USERNAME_KEY, username);
        }
    }

    // OAuth2 제공자별 사용자 이름/이메일 추출 (사용자 정보 출력에 email 을 key 값으로 차용)
    private String extractOAuth2Username(OAuth2User oauth2User, String provider) {
        String username = null;

        try {
            if ("google".equals(provider)) {
                username = oauth2User.getAttribute("email");
            } else if ("kakao".equals(provider)) {
                Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                    username = (String) kakaoAccount.get("email");
                } else {
                    username = "kakao-user-" + oauth2User.getAttribute("id");
                }
            } else if ("naver".equals(provider)) {
                Map<String, Object> response = oauth2User.getAttribute("response");
                if (response != null && response.containsKey("email")) {
                    username = (String) response.get("email");
                } else if (response != null && response.containsKey("id")) {
                    username = "naver-user-" + response.get("id");
                } else {
                    username = "naver-user-unknown";
                }
            } else {
                username = "oauth2-user-" + System.currentTimeMillis();
            }
        } catch (Exception e) {
            username = "oauth2-user-error";
        }

        return username;
    }

    // 세션에서 인증 제공자 정보 가져오기
    public String getProviderFromSession(HttpSession session) {
        return (String) session.getAttribute(AUTH_PROVIDER_KEY);
    }

    // 세션에서 사용자명 가져오기
    public String getUsernameFromSession(HttpSession session) {
        return (String) session.getAttribute(AUTH_USERNAME_KEY);
    }
}