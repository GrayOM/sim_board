package com.sim.board.config.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class Oauth2_login_failure_handler extends SimpleUrlAuthenticationFailureHandler {
//Oauth2 인증 실패시 호출되는 클래스
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String targetUrl = UriComponentsBuilder.fromUriString("/login") //로그인 페이지로 리다렉트
                .queryParam("error", "oauth2_error") //Oauth2 인증 오류 표시
                .queryParam("message", exception.getLocalizedMessage()) // 오류 상세 메세지
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}