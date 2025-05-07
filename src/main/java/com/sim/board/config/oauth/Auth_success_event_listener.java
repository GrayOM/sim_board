package com.sim.board.config.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class Auth_success_event_listener { //인증 성공 , 인증 정보 세션에 저장시키는 클래스

    private final User_session_manager usersessionmanager;

    @EventListener
    public void onAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) {
        //인증 객체를 이벤트로부터 가져오기
        Authentication authentication = event.getAuthentication();

        // 현재 요청에서 세션을 가져오기 위한 객체
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) { //http 요청 속성 키-값 있는경우
            HttpServletRequest request = attributes.getRequest(); //현재 http 요청을 가져오기
            HttpSession session = request.getSession(true);  //없으면 세션 가져오기

            // 인증 제공자 정보를 세션에 저장
            usersessionmanager.saveProviderToSession(session, authentication);
        }
    }
}