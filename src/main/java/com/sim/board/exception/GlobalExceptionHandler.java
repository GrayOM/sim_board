// src/main/java/com/sim/board/exception/GlobalExceptionHandler.java
package com.sim.board.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.logging.Logger;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    // 보안 예외 처리
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleSecurityException(SecurityException ex, Model model, HttpServletRequest request) {
        logger.warning("보안 예외 발생: " + ex.getMessage() + " - 요청 경로: " + request.getRequestURI());
        model.addAttribute("error", "보안 오류: " + ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/security";
    }

    // 인증 예외 처리
    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthenticationException(AuthenticationException ex, HttpServletRequest request,
                                                RedirectAttributes redirectAttributes) {
        logger.warning("인증 예외 발생: " + ex.getMessage() + " - 요청 경로: " + request.getRequestURI());

        String errorMessage = "인증에 실패했습니다.";
        if (ex instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
        } else if (ex instanceof LockedException) {
            errorMessage = "계정이 잠겼습니다. 나중에 다시 시도하세요.";
        } else if (ex instanceof DisabledException) {
            errorMessage = "계정이 비활성화되었습니다. 관리자에게 문의하세요.";
        }

        redirectAttributes.addFlashAttribute("error", errorMessage);
        return "redirect:/login?error";
    }

    // 접근 거부 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(AccessDeniedException ex, Model model, HttpServletRequest request) {
        logger.warning("접근 거부 예외 발생: " + ex.getMessage() + " - 요청 경로: " + request.getRequestURI());
        model.addAttribute("error", "접근 권한이 없습니다.");
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/access-denied";
    }

    // 파일 업로드 크기 초과 예외 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex,
                                              RedirectAttributes redirectAttributes,
                                              HttpServletRequest request) {
        logger.warning("파일 업로드 크기 초과: " + request.getRequestURI());
        redirectAttributes.addFlashAttribute("error", "파일 크기가 허용된 최대 크기를 초과했습니다.");

        // 요청 경로에 따라 리다이렉트 처리
        if (request.getRequestURI().contains("/boards/write")) {
            return "redirect:/boards/write";
        } else if (request.getRequestURI().contains("/boards/") && request.getRequestURI().contains("/edit")) {
            String[] pathParts = request.getRequestURI().split("/");
            Long boardId = null;
            for (int i = 0; i < pathParts.length; i++) {
                if ("boards".equals(pathParts[i]) && i+1 < pathParts.length) {
                    try {
                        boardId = Long.parseLong(pathParts[i+1]);
                        break;
                    } catch (NumberFormatException ignored) {}
                }
            }
            return "redirect:/boards/" + (boardId != null ? boardId + "/edit" : "");
        }

        return "redirect:/boards";
    }

    // 일반 런타임 예외 처리
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException ex, Model model, HttpServletRequest request) {
        logger.severe("런타임 예외 발생: " + ex.getMessage() + " - 요청 경로: " + request.getRequestURI());
        ex.printStackTrace();
        model.addAttribute("error", "서버 오류가 발생했습니다: " + ex.getMessage());
        model.addAttribute("requestUri", request.getRequestURI());
        return "error/error";
    }
}