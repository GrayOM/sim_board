package com.sim.board.config.oauth.userinfo;
//google userinfo 생성자
import java.util.Map;

public class GoogleUserInfo extends OAuth2UserInfo {

    public GoogleUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId(){
        return attributes.get("sub").toString(); //google 사용자 고유 ID 반환
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    } //이름 반환

    @Override
    public String getEmail() {
        return attributes.get("email").toString(); //email 반환
    }

    @Override
    public String getImageUrl() {
        return attributes.getOrDefault("picture", "").toString();
    }//이미지 반환

    @Override
    public String getProvider() {
        return "google";
    }
}