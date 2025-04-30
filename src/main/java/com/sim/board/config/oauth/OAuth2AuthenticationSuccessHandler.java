package com.sim.board.config.oauth;

import com.sim.board.config.oauth.userinfo.OAuth2UserInfo;
import com.sim.board.config.oauth.userinfo.OAuth2UserInfoFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        // OAuth2 인증인 경우 사용자 이메일 정보를 리다이렉트 URL에 포함
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            // 소셜 로그인 제공자별 사용자 정보 추출
            Map<String, Object> attributes = oAuth2User.getAttributes();
            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

            // 소셜 로그인 제공자와 이메일 정보를 쿼리 파라미터로 추가
            String email = userInfo.getEmail();
            String redirectUrl = "/boards?login=true&provider=" + registrationId;

            if (email != null && !email.isEmpty()) {
                redirectUrl += "&username=" + email;
            }

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else {
            // 일반 로그인인 경우 기존 리다이렉트 주소 사용
            getRedirectStrategy().sendRedirect(request, response, "/boards?login=true");
        }
    }
}