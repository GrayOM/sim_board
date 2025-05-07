package com.sim.board.config.oauth.userinfo;

import java.util.Map;

public class Google_User_Info extends OAuth2_UserInfo {
//OAuth2userinfo 를 확장하여 google oauth2 사용자 정보 처리
    public Google_User_Info(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId(){ //Google Oauth2 응답에서 sub 필드가 사용자 id 로 사용
        return attributes.get("sub").toString();
    }

    @Override
    public String getName() {  //name,email , image , 제공자 이름으로 반환시킴
        return attributes.get("name").toString();
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getImageUrl() {
        return attributes.getOrDefault("picture", "").toString();
    }

    @Override
    public String getProvider() {
        return "google";
    }
}