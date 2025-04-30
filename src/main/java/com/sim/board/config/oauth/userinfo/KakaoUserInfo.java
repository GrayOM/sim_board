package com.sim.board.config.oauth.userinfo;

import java.util.HashMap;
import java.util.Map;

public class KakaoUserInfo extends OAuth2UserInfo {

    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getName() {
        Map<String, Object> properties = getProperties();
        if (properties == null) {
            return "Kakao User";
        }
        return properties.getOrDefault("nickname", "Kakao User").toString();
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        if (kakaoAccount == null) {
            return "";
        }
        return kakaoAccount.getOrDefault("email", "").toString();
    }

    @Override
    public String getImageUrl() {
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