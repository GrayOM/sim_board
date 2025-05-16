// src/main/java/com/sim/board/controller/PasswordController.java
package com.sim.board.controller;

import com.sim.board.domain.user;
import com.sim.board.dto.PasswordChangeDto;
import com.sim.board.service.auth_service;
import com.sim.board.service.user_service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class PasswordController {

    private final user_service userService;
    private final auth_service authService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/change-password")
    public String changePasswordForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto passwordChangeDto,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/change-password";
        }

        // 사용자 정보 가져오기
        String username = authService.extractUsername(authentication);
        user user = userService.getUserByUsername(username);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            bindingResult.rejectValue("currentPassword", "error.currentPassword", "현재 비밀번호가 일치하지 않습니다.");
            return "user/change-password";
        }

        // 새 비밀번호가 이전 비밀번호와 같은지 확인
        if (passwordEncoder.matches(passwordChangeDto.getNewPassword(), user.getPassword())) {
            bindingResult.rejectValue("newPassword", "error.newPassword", "새 비밀번호는 현재 비밀번호와 다르게 설정해야 합니다.");
            return "user/change-password";
        }

        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "user/change-password";
        }

        // 비밀번호 업데이트
        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userService.updateUser(user);

        redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
        return "redirect:/boards";
    }
}