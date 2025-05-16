// src/main/java/com/sim/board/config/FilterConfig.java
package com.sim.board.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    private final SecurityHeadersFilter securityHeadersFilter;
    private final PasswordExpirationFilter passwordExpirationFilter;

    public FilterConfig(SecurityHeadersFilter securityHeadersFilter,
                        PasswordExpirationFilter passwordExpirationFilter) {
        this.securityHeadersFilter = securityHeadersFilter;
        this.passwordExpirationFilter = passwordExpirationFilter;
    }

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>(securityHeadersFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE); // 가장 높은 우선순위
        return registration;
    }

    @Bean
    public FilterRegistrationBean<PasswordExpirationFilter> passwordExpirationFilterRegistration() {
        FilterRegistrationBean<PasswordExpirationFilter> registration = new FilterRegistrationBean<>(passwordExpirationFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10); // 보안 헤더 필터 다음 순서
        return registration;
    }
}