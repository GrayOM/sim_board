package com.sim.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableJdbcHttpSession(
        maxInactiveIntervalInSeconds = 1800, // 30분 세션 유효시간
        tableName = "SPRING_SESSION"
)
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setSameSite("Strict"); // CSRF 방어를 위한 더 강력한 SameSite 설정
        serializer.setUseSecureCookie(true); // HTTPS에서만 쿠키 전송
        serializer.setUseHttpOnlyCookie(true); // JavaScript에서 쿠키 접근 방지
        serializer.setCookieName("BOARDSESSION"); // 기본 JSESSIONID 대신 커스텀 이름 사용
        serializer.setCookiePath("/"); // 모든 경로에서 쿠키 사용 가능
        serializer.setCookieMaxAge(1800); // 쿠키 만료 시간 (30분)
        return serializer;
    }
}