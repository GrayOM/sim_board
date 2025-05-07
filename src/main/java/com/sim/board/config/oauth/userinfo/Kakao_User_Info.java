package com.sim.board.config.oauth.userinfo;

import java.util.HashMap;
import java.util.Map;

public class Kakao_User_Info extends OAuth2_UserInfo {
//OAuth2 사용자 정보 처리 kakao
    public Kakao_User_Info(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() { //카카오는 id 필드가 사용된다 사용자 id 가
        return attributes.get("id").toString();
    }

    @Override
    public String getName() { //이름,이메일,이미지URL를 반환 없으면 NULL 값으로 채움
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
    private Map<String, Object> getProperties() { //properties , kakaoaccount 필드를 가져옴 없으면 안가져옴
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