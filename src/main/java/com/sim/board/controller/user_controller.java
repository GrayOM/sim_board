package com.sim.board.controller;

import com.sim.board.domain.user;
import com.sim.board.service.user_service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class user_controller {

    private final user_service userService;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm() {
        return "user/login";
    }

    // 회원가입 페이지
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new user());
        return "user/register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String register(@ModelAttribute user user) {
        userService.register(user);
        return "redirect:/login";
    }
}