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

        if (principal instanceof OAuth2User && provider != null) {
            // OAuth2 로그인 사용자
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) principal;

            // OAuth2 제공자 정보 (google, kakao, naver)를 설정
            redirectAttributes.addAttribute("provider", provider);

            // 사용자명을 가져옵니다. 제공자마다 속성 구조가 다를 수 있습니다.
            Map<String, Object> attributes = ((OAuth2User) principal).getAttributes();

            // 이메일 또는 다른 고유 식별자를 사용자명으로 사용
            if (provider.equals("google")) {
                username = attributes.get("email").toString();
            } else if (provider.equals("kakao")) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                username = kakaoAccount.get("email").toString();
            } else if (provider.equals("naver")) {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                username = response.get("email").toString();
            } else {
                username = "oauth2-user"; // 기본값
            }

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