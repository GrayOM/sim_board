package com.sim.board.config.oauth.userinfo;

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
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            return "Unknown";
        }
        return properties.getOrDefault("nickname", "Unknown").toString();
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return "";
        }
        return kakaoAccount.getOrDefault("email", "").toString();
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            return "";
        }
        return properties.getOrDefault("profile_image", "").toString();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }
}