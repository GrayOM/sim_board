// src/main/java/com/sim/board/config/SessionConfig.java
package com.sim.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800) // 30분 세션 유효시간
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setSameSite("Lax"); // CSRF 방어 위한 SameSite 설정
        serializer.setUseSecureCookie(true); // HTTPS에서만 쿠키 전송
        serializer.setUseHttpOnlyCookie(true); // JavaScript에서 쿠키 접근 방지
        return serializer;
    }
}