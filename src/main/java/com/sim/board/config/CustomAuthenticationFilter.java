package com.sim.board.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final LoginAttemptService loginAttemptService;
    private boolean postOnly = true;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager,
                                      LoginAttemptService loginAttemptService) {
        super(authenticationManager);
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("인증 메소드가 지원되지 않습니다: " + request.getMethod());
        }

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        // IP 기반 로그인 시도 횟수 체크
        String ip = getClientIP(request);
        if (loginAttemptService.isBlocked(ip)) {
            throw new AuthenticationServiceException("너무 많은 로그인 시도로 1시간 동안 차단되었습니다.");
        }

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, password);
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}