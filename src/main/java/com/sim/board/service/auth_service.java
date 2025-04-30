// src/main/java/com/sim/board/service/auth_service.java 파일의 추가 메서드

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
            try {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

                // 소셜 로그인 제공자별 사용자 정보 추출
                if ("google".equals(provider)) {
                    String email = oauth2User.getAttribute("email");
                    if (email != null) {
                        return email;
                    }
                    // 이메일이 없는 경우 기본값 사용
                    return "google-user-" + oauth2User.getName();
                } else if ("kakao".equals(provider)) {
                    try {
                        Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                            return (String) kakaoAccount.get("email");
                        } else {
                            // 이메일이 없는 경우 ID 기반 사용자명 생성
                            Object id = oauth2User.getAttribute("id");
                            return "kakao-user-" + (id != null ? id.toString() : "unknown");
                        }
                    } catch (Exception e) {
                        // 예외 발생 시 기본값 사용
                        return "kakao-user-" + System.currentTimeMillis();
                    }
                } else if ("naver".equals(provider)) {
                    try {
                        Map<String, Object> response = oauth2User.getAttribute("response");
                        if (response != null && response.containsKey("email")) {
                            return (String) response.get("email");
                        } else if (response != null && response.containsKey("id")) {
                            return "naver-user-" + response.get("id");
                        } else {
                            // 정보가 없는 경우 기본값 사용
                            return "naver-user-" + System.currentTimeMillis();
                        }
                    } catch (Exception e) {
                        // 예외 발생 시 기본값 사용
                        return "naver-user-" + System.currentTimeMillis();
                    }
                } else {
                    // 지원하지 않는 제공자인 경우 기본값 사용
                    return "oauth2-user-" + System.currentTimeMillis();
                }
            } catch (Exception e) {
                // OAuth2 처리 중 예외 발생 시 기본값 사용
                return "oauth2-user-" + System.currentTimeMillis();
            }
        }
        // 일반 로그인인 경우
        else if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }

        // 기타 경우
        return authentication.getName();
    }

    /**
     * OAuth2 사용자인지 확인합니다.
     */
    public boolean isOAuth2User(Authentication authentication) {
        return authentication != null &&
                authentication.getPrincipal() instanceof OAuth2User;
    }

    /**
     * 인증 객체로부터 이메일을 추출합니다.
     * OAuth2 인증과 일반 인증 모두 처리합니다.
     */
    public String extractEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        // OAuth2 로그인인 경우
        if (authentication instanceof OAuth2AuthenticationToken) {
            try {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

                // 소셜 로그인 제공자별 이메일 추출
                if ("google".equals(provider)) {
                    return oauth2User.getAttribute("email");
                } else if ("kakao".equals(provider)) {
                    Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                    if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                        return (String) kakaoAccount.get("email");
                    }
                } else if ("naver".equals(provider)) {
                    Map<String, Object> response = oauth2User.getAttribute("response");
                    if (response != null && response.containsKey("email")) {
                        return (String) response.get("email");
                    }
                }
            } catch (Exception e) {
                System.err.println("OAuth2 이메일 추출 중 오류: " + e.getMessage());
            }
            return null;
        }
        // 일반 사용자의 경우 이메일은 별도 처리 필요
        return null;
    }
}