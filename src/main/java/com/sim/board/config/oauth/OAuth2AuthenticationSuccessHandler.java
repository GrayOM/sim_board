package com.sim.board.config.oauth;

import com.sim.board.config.oauth.userinfo.OAuth2UserInfo;
import com.sim.board.config.oauth.userinfo.OAuth2UserInfoFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
//Oauth2 로그인 시도 성공햇을때 성공 핸들러
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticationSessionService authSessionService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        HttpSession session = request.getSession(true);

        // OAuth2 인증인 경우 사용자 이메일 정보를 세션과 리다이렉트 URL에 포함
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            // 소셜 로그인 제공자별 사용자 정보 추출
            Map<String, Object> attributes = oAuth2User.getAttributes();
            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

            // 세션에 인증 정보 저장
            session.setAttribute("auth_provider", registrationId);

            // 이메일 정보를 세션에 저장
            String email = userInfo.getEmail();
            if (email != null && !email.isEmpty()) {
                session.setAttribute("auth_username", email);

                // 사용자 정보 확인 로직 간소화 - 순환 참조 방지
                System.out.println("OAuth2 사용자 이메일: " + email);
            } else {
                // 이메일이 없는 경우 고유 ID 기반 사용자명 생성
                String username = registrationId + "-user-" + userInfo.getId();
                session.setAttribute("auth_username", username);
            }

            // 디버깅 로그
            System.out.println("OAuth2 로그인 성공 - Provider: " + registrationId);
            System.out.println("OAuth2 로그인 성공 - Email: " + email);
            System.out.println("OAuth2 로그인 성공 - ID: " + userInfo.getId());

            // 리다이렉트 URL에도 정보 포함 (하위 호환성 유지)
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