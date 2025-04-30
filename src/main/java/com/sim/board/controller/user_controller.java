package com.sim.board.controller;

import com.sim.board.domain.user;
import com.sim.board.service.user_service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class user_controller {

    private final user_service userService;

    // 인덱스 페이지
    @GetMapping("/")
    public String indexform() {
        return "redirect:/boards";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "message", required = false) String message,
                            Model model) {
        // 이미 로그인한 사용자는 게시판 목록으로 리다이렉트
        if (userDetails != null) {
            return "redirect:/boards";
        }

        // OAuth 로그인 오류 메시지 추가
        if (error != null && error.equals("oauth2_error")) {
            model.addAttribute("oauth2Error", true);
            model.addAttribute("errorMessage", message);
        }

        return "user/login";
    }

    // 로그인 성공 처리 (OAuth 및 일반 로그인)
    @GetMapping("/login-success")
    public String loginSuccess(@AuthenticationPrincipal Object principal,
                               @RequestParam(value = "provider", required = false) String provider,
                               RedirectAttributes redirectAttributes) {

        String username;
        String email = null;

        if (principal instanceof OAuth2User) {
            // OAuth2 로그인 사용자
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;

            // 제공자 정보가 파라미터로 없다면 토큰에서 가져옴
            if (provider == null) {
                provider = token.getAuthorizedClientRegistrationId();
            }

            // OAuth2 제공자 정보 (google, kakao, naver)를 설정
            redirectAttributes.addAttribute("provider", provider);

            // 사용자명을 가져옵니다. 제공자마다 속성 구조가 다를 수 있습니다.
            Map<String, Object> attributes = ((OAuth2User) principal).getAttributes();

            // 각 소셜 로그인 제공자별로 이메일 추출
            if ("google".equals(provider)) {
                email = (String) attributes.get("email");
                username = email;
            } else if ("kakao".equals(provider)) {
                // 안전한 형변환을 위해 instanceof 검사 추가
                Object kakaoAccountObj = attributes.get("kakao_account");
                if (kakaoAccountObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;
                    if (kakaoAccount.containsKey("email")) {
                        email = (String) kakaoAccount.get("email");
                        username = email;
                    } else {
                        username = "kakao-user-" + attributes.get("id");
                    }
                } else {
                    username = "kakao-user-" + attributes.get("id");
                }
            } else if ("naver".equals(provider)) {
                // 안전한 형변환을 위해 instanceof 검사 추가
                Object responseObj = attributes.get("response");
                if (responseObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) responseObj;
                    if (response.containsKey("email")) {
                        email = (String) response.get("email");
                        username = email;
                    } else {
                        username = "naver-user-" + response.get("id");
                    }
                } else {
                    username = "naver-user-unknown";
                }
            } else {
                username = "oauth2-user-" + System.currentTimeMillis(); // 기본값
            }

            // 디버깅 로그
            System.out.println("OAuth2 로그인 - Provider: " + provider);
            System.out.println("OAuth2 로그인 - Email: " + email);
            System.out.println("OAuth2 로그인 - Username: " + username);

        } else if (principal instanceof UserDetails) {
            // 일반 로그인 사용자
            username = ((UserDetails) principal).getUsername();
        } else {
            // 기타 경우 (비정상)
            return "redirect:/login?error";
        }

        redirectAttributes.addAttribute("login", true);
        redirectAttributes.addAttribute("username", username);

        return "redirect:/boards";
    }
    // 회원가입 페이지
    @GetMapping("/register")
    public String registerForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // 이미 로그인한 사용자는 게시판 목록으로 리다이렉트
        if (userDetails != null) {
            return "redirect:/boards";
        }
        model.addAttribute("user", new user());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute user user, Model model, RedirectAttributes redirectAttributes) {
        try {
            userService.register(user);
            redirectAttributes.addFlashAttribute("registrationSuccess", "회원가입이 성공적으로 완료되었습니다.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "user/register";
        }
    }
}