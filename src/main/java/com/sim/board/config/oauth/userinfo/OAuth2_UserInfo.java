package com.sim.board.config.oauth.userinfo;
//OAuth2userinfo 클래스는 Oauth2인증 (구글,네이버,카카오) 사용자 정보 처리하기 위한 확장 전 클래스
import java.util.Map;

public abstract class OAuth2_UserInfo {

    protected Map<String, Object> attributes;

    public OAuth2_UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getId(); //ID,name,email,imageURL,제공했을때 이름 를 구현해야함

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getImageUrl();

    public abstract String getProvider();
}