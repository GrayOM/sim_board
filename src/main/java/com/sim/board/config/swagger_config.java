package com.sim.board.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class swagger_config {

    @Bean
    public OpenAPI openAPI() {
        // 기본 API 정보 설정
        Info info = new Info()
                .title("Spring Boot 게시판 API 문서")
                .description("Spring Boot와 MySQL을 활용한 게시판 웹 애플리케이션의 API 문서입니다.")
                .version("1.0.0")
                .contact(new Contact()
                        .url("https://github.com/GrayOM/sim_board"));

        // Google OAuth 보안 스키마 설정
        SecurityScheme googleOAuth2 = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Google OAuth2 인증")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("https://accounts.google.com/o/oauth2/auth")
                                .tokenUrl("https://oauth2.googleapis.com/token")
                                .scopes(new Scopes()
                                        .addString("email", "이메일 정보 접근")
                                        .addString("profile", "프로필 정보 접근"))));

        // Kakao OAuth 보안 스키마 설정
        SecurityScheme kakaoOAuth2 = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Kakao OAuth2 인증")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("https://kauth.kakao.com/oauth/authorize")
                                .tokenUrl("https://kauth.kakao.com/oauth/token")
                                .scopes(new Scopes()
                                        .addString("profile_nickname", "닉네임 정보 접근")
                                        .addString("profile_image", "프로필 이미지 정보 접근")
                                        .addString("account_email", "이메일 정보 접근"))));

        // Naver OAuth 보안 스키마 설정
        SecurityScheme naverOAuth2 = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Naver OAuth2 인증")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("https://nid.naver.com/oauth2.0/authorize")
                                .tokenUrl("https://nid.naver.com/oauth2.0/token")
                                .scopes(new Scopes()
                                        .addString("name", "이름 정보 접근")
                                        .addString("email", "이메일 정보 접근")
                                        .addString("profile_image", "프로필 이미지 정보 접근"))));

        // JWT 토큰 인증 스키마 설정 (OAuth 인증 후 사용할 토큰)
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 보안 요구사항 설정
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("googleOAuth2")
                .addList("kakaoOAuth2")
                .addList("naverOAuth2")
                .addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes("googleOAuth2", googleOAuth2)
                        .addSecuritySchemes("kakaoOAuth2", kakaoOAuth2)
                        .addSecuritySchemes("naverOAuth2", naverOAuth2)
                        .addSecuritySchemes("bearerAuth", jwtScheme))
                .addSecurityItem(securityRequirement);
    }
}