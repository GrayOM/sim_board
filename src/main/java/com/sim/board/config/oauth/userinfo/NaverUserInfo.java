package com.sim.board.config.oauth.userinfo;

import java.util.HashMap;
import java.util.Map;

public class NaverUserInfo extends OAuth2UserInfo {
//naver 인증서버에서 받은 사용자 정보 처리
    public NaverUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() { //ID
        Map<String, Object> response = getResponse();
        if (response == null) {
            return null;
        }
        return response.get("id").toString();
    }

    @Override
    public String getName() { //name
        Map<String, Object> response = getResponse();
        if (response == null) {
            return "Naver User";
        }
        return response.getOrDefault("name", "Naver User").toString();
    }

    @Override
    public String getEmail() { //email
        Map<String, Object> response = getResponse();
        if (response == null) {
            return "";
        }
        return response.getOrDefault("email", "").toString();
    }

    @Override
    public String getImageUrl() { //프로필 이미지
        Map<String, Object> response = getResponse();
        if (response == null) {
            return "";
        }
        return response.getOrDefault("profile_image", "").toString();
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getResponse() {
        try {
            return (Map<String, Object>) attributes.get("response");
        } catch (ClassCastException e) {
            return new HashMap<>();
        }
    }
}