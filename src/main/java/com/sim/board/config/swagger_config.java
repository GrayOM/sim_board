package com.sim.board.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class swagger_config {

    @Bean
    public OpenAPI openAPI() {
        // 기본 API 정보 설정
        Info info = new Info()
                .title("Spring Boot 게시판 API 문서") //API 제목 창
                .description("Spring Boot 3.4.4와 MySQL을 활용한 게시판 웹 애플리케이션의 API 문서입니다. " +
                        "Google, Kakao, Naver OAuth2 로그인을 지원합니다.") //API 설명 창
                .contact(new Contact()
                        .url("https://github.com/GrayOM/sim_board")); //연락처 (Github 링크)

        // JWT Bearer 인증 스키마 설정
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) //HTTP 타입 인증 방식
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER) //http 헤더에서 인증 정보를 전달시킴
                .name("Authorization");

        // OAuth2 인증 스키마 설정 - Google
        SecurityScheme googleOAuth2Scheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Google OAuth2 인증")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("https://accounts.google.com/o/oauth2/auth")
                                .tokenUrl("https://oauth2.googleapis.com/token")
                                .scopes(new io.swagger.v3.oas.models.security.Scopes() //Google Oauth2에서 요청할 권한 설정
                                        .addString("email", "이메일 정보 접근")
                                        .addString("profile", "프로필 정보 접근"))));

        // OAuth2 인증 스키마 설정 - Kakao
        SecurityScheme kakaoOAuth2Scheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Kakao OAuth2 인증")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("https://kauth.kakao.com/oauth/authorize")
                                .tokenUrl("https://kauth.kakao.com/oauth/token")
                                .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                        .addString("profile_nickname", "닉네임 정보 접근")
                                        .addString("profile_image", "프로필 이미지 접근")
                                        .addString("account_email", "이메일 정보 접근"))));

        // OAuth2 인증 스키마 설정 - Naver
        SecurityScheme naverOAuth2Scheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Naver OAuth2 인증")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("https://nid.naver.com/oauth2.0/authorize")
                                .tokenUrl("https://nid.naver.com/oauth2.0/token")
                                .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                        .addString("name", "이름 정보 접근")
                                        .addString("email", "이메일 정보 접근")
                                        .addString("profile_image", "프로필 이미지 접근"))));

        // 기본 보안 요구사항 설정
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth")
                .addList("googleOAuth2")
                .addList("kakaoOAuth2")
                .addList("naverOAuth2");

        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerScheme)
                        .addSecuritySchemes("googleOAuth2", googleOAuth2Scheme)
                        .addSecuritySchemes("kakaoOAuth2", kakaoOAuth2Scheme)
                        .addSecuritySchemes("naverOAuth2", naverOAuth2Scheme))
                .addSecurityItem(securityRequirement);
    }
}