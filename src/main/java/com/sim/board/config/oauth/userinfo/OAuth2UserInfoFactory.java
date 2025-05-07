package com.sim.board.config.oauth.userinfo;

import java.util.Map;
//Oauth2 에서 가져오는 사용자인증 정보가 이상하거나 다른 Oauth2 아이디로 시도할라고할떄
public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId == null) {
            throw new IllegalArgumentException("소셜 로그인 제공자 ID가 null입니다.");
        }

        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("속성 맵이 null이거나 비어 있습니다.");
        }

        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            return new KakaoUserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase("naver")) {
            return new NaverUserInfo(attributes);
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }
    }
}