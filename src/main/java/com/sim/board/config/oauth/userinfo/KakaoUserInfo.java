package com.sim.board.config.oauth.userinfo;

import java.util.HashMap;
import java.util.Map;
//kakao 인증 서버로부터 받는 사용자 정보 처리
public class KakaoUserInfo extends OAuth2UserInfo {

    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() { //사용자 Id 반환
        return attributes.get("id").toString();
    }

    @Override
    public String getName() { //이름반환 없을시 kakaouser
        Map<String, Object> properties = getProperties();
        if (properties == null) {
            return "Kakao User";
        }
        return properties.getOrDefault("nickname", "Kakao User").toString();
    }

    @Override
    public String getEmail() { //사용자 이메일
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) {
            return "";
        }
        return kakaoAccount.getOrDefault("email", "").toString();
    }

    @Override
    public String getImageUrl() { //이미지 반환
        Map<String, Object> properties = getProperties();
        if (properties == null) {
            return "";
        }
        return properties.getOrDefault("profile_image", "").toString();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getProperties() {
        try {
            return (Map<String, Object>) attributes.get("properties");
        } catch (ClassCastException e) {
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoAccount() {
        try {
            return (Map<String, Object>) attributes.get("kakao_account");
        } catch (ClassCastException e) {
            return new HashMap<>();
        }
    }
}