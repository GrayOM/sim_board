package com.sim.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 설정 클래스 선언
@EnableWebSecurity //spring security를 활성화
public class security_config {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화 (API 서버의 경우)
                .authorizeHttpRequests(auth -> auth //인증,인가 설정
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()  // 각 경로들은 인가가 별도로 설정되지않음
                        .requestMatchers("/api/**").permitAll()  // api 로 시작하는 경로도 따로 인가없이 접근 허용
                        .anyRequest().authenticated()  // 위에 설정을 제외한 모든 요청은 부여받은 인가나 관리자 권한 인증이 필요함
                )
                .formLogin(login -> login
                        .loginPage("/login")  // 로그인  경로 설정
                        .defaultSuccessUrl("/boards", true)  // 로그인 성공시 접속 경로
                        .permitAll() // 로그인 페이지에 대한 접근은 모두 허용 시킴
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")  // 로그아웃 성공 시 경로
                        .permitAll() // 로그아웃 요청은 모두 허용 시킴
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 비밀번호는 단방향 해시 알고리즘인 Bcrypt 를 이용하여 암호화
    }
}