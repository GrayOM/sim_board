package com.sim.board.controller;

import com.sim.board.domain.user;
import com.sim.board.service.user_service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String loginForm(@AuthenticationPrincipal UserDetails userDetails) {
        // 이미 로그인한 사용자는 게시판 목록으로 리다이렉트
        if (userDetails != null) {
            return "redirect:/boards";
        }
        return "user/login";
    }

    // 로그인 성공 처리 (Spring Security 설정에서 defaultSuccessUrl 수정 필요)
    @GetMapping("/login-success")
    public String loginSuccess(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("login", true);
        redirectAttributes.addAttribute("username", userDetails.getUsername());
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