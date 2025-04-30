package com.sim.board.config.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@RequiredArgsConstructor
public class AuthenticationControllerAdvice {

    private final AuthenticationSessionService authSessionService;

    // 모든 컨트롤러 메서드가 실행되기 전에 호출되어 필요한 세션 데이터를 모델에 추가
    @ModelAttribute
    public void addAuthDataToModel(HttpServletRequest request, Model model) {
        // 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            HttpSession session = request.getSession(true);

            // URL 파라미터로 provider가 전달된 경우 세션에 저장
            String providerParam = request.getParameter("provider");
            if (providerParam != null && !providerParam.isEmpty()) {
                session.setAttribute("auth_provider", providerParam);
            }

            // 현재 인증 정보에서 OAuth 제공자 타입 확인
            authSessionService.saveProviderToSession(session, authentication);

            // 사용자 이름 저장
            String username = "";

            // OAuth2 로그인인 경우
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

                // 소셜 로그인 제공자별 사용자 정보 추출
                if ("google".equals(provider)) {
                    username = oauth2User.getAttribute("email");
                } else if ("kakao".equals(provider)) {
                    Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                    if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                        username = (String) kakaoAccount.get("email");
                    } else {
                        username = "kakao-user-" + oauth2User.getAttribute("id");
                    }
                } else if ("naver".equals(provider)) {
                    Map<String, Object> response = oauth2User.getAttribute("response");
                    if (response != null && response.containsKey("email")) {
                        username = (String) response.get("email");
                    } else {
                        username = "naver-user-" + Objects.requireNonNull(response).get("id");
                    }
                }
            } else if (authentication.getPrincipal() instanceof UserDetails) {
                // 일반 로그인인 경우
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getName();
            }

            // 세션에 사용자 이름 저장
            session.setAttribute("auth_username", username);

            // 모델에도 추가하여 뷰에서 사용할 수 있게 함
            model.addAttribute("authUsername", username);
            model.addAttribute("authProvider", session.getAttribute("auth_provider"));
        }
    }
}