package com.sim.board.config.oauth.userinfo;
//Oauth2 인증을 사용자 정보를 board 서비스에 맞게 통합 처리
import java.util.Map;

public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    } //생성자

    public abstract String getId(); //id,name,email,imageurl

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getImageUrl();

    public abstract String getProvider(); //각 제공자별(네이버,구글,카카오) 이름이 달러서 따로 구현
}