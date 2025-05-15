// src/main/java/com/sim/board/controller/user_controller.java (수정)
package com.sim.board.controller;

import com.sim.board.domain.user;
import com.sim.board.dto.UserRegisterDto;
import com.sim.board.service.user_service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    // 회원가입 페이지
    @GetMapping("/register")
    public String registerForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // 이미 로그인한 사용자는 게시판 목록으로 리다이렉트
        if (userDetails != null) {
            return "redirect:/boards";
        }
        model.addAttribute("userDto", new UserRegisterDto());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userDto") UserRegisterDto userDto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // 입력값 검증 오류 처리
        if (bindingResult.hasErrors()) {
            return "user/register";
        }

        try {
            // DTO를 엔티티로 변환
            user newUser = new user();
            newUser.setUsername(userDto.getUsername());
            newUser.setPassword(userDto.getPassword());
            newUser.setName(userDto.getName());
            newUser.setEmail(userDto.getEmail());

            userService.register(newUser);
            redirectAttributes.addFlashAttribute("registrationSuccess", "회원가입이 성공적으로 완료되었습니다.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "user/register";
        }
    }

    // 다른 메서드는 그대로 유지...
}