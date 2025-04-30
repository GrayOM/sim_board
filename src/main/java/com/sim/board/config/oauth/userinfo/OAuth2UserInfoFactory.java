package com.sim.board.config.oauth.userinfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equals("google")) {
            return new GoogleUserInfo(attributes);
        } else if (registrationId.equals("kakao")) {
            return new KakaoUserInfo(attributes);
        } else if (registrationId.equals("naver")) {
            return new NaverUserInfo(attributes);
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }
    }
}