package com.sim.board.config.oauth.userinfo;

import java.util.Map;

public class NaverUserInfo extends OAuth2UserInfo {

    public NaverUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return null;
        }
        return response.get("id").toString();
    }

    @Override
    public String getName() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return "Unknown";
        }
        return response.getOrDefault("name", "Unknown").toString();
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return "";
        }
        return response.getOrDefault("email", "").toString();
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            return "";
        }
        return response.getOrDefault("profile_image", "").toString();
    }

    @Override
    public String getProvider() {
        return "naver";
    }
}