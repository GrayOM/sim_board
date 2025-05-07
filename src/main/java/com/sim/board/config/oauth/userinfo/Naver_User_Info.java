package com.sim.board.config.oauth.userinfo;

import java.util.HashMap;
import java.util.Map;

public class Naver_User_Info extends OAuth2_UserInfo {
//네이버 userinfo OAuth2 로 확장
    public Naver_User_Info(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() { //네이버 사용자 정보는 response 안에 존재함
        Map<String, Object> response = getResponse();
        if (response == null) {
            return null;
        }
        return response.get("id").toString();
    }

    @Override
    public String getName() { //사용자 이름 반환
        Map<String, Object> response = getResponse();
        if (response == null) {
            return "Naver User";
        }
        return response.getOrDefault("name", "Naver User").toString();
    }

    @Override
    public String getEmail() { //쓰고 있는 이메일 반환 (key 값으로 설정할것)
        Map<String, Object> response = getResponse();
        if (response == null) {
            return "";
        }
        return response.getOrDefault("email", "").toString();
    }

    @Override
    public String getImageUrl() { //이미지URL (프로필 사진)
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