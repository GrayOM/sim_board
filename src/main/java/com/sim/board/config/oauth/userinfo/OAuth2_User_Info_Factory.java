package com.sim.board.config.oauth.userinfo;

import java.util.Map;
//Oauth에 따라(google,naver,kakao) 객체 반환 팩토리 클래스
//Oauth 인증 과정에서 받은 사용자 키-값 쌍 정보 반환
public class OAuth2_User_Info_Factory {

    public static OAuth2_UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId == null) {
            throw new IllegalArgumentException("소셜 로그인 제공자 ID가 null입니다.");
        }

        if (attributes == null || attributes.isEmpty()) {
            throw new IllegalArgumentException("속성 맵이 null이거나 비어 있습니다.");
        }

        if (registrationId.equalsIgnoreCase("google")) {
            return new Google_User_Info(attributes);
        } else if (registrationId.equalsIgnoreCase("kakao")) {
            return new Kakao_User_Info(attributes);
        } else if (registrationId.equalsIgnoreCase("naver")) {
            return new Naver_User_Info(attributes);
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }
    }
}