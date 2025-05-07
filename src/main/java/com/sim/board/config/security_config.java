package com.sim.board.config;

import com.sim.board.config.oauth.Oauth2_user_details_service;
import com.sim.board.config.oauth.Oauth2_login_failure_handler;
import com.sim.board.config.oauth.Oauth2_login_success_handler;
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

    private final Oauth2_user_details_service oauth2Userdetailsservice;
    private final Oauth2_login_success_handler oauth2Loginsuccesshandler;
    private final Oauth2_login_failure_handler oauth2Loginfailurehandler;

    public security_config(Oauth2_user_details_service oauth2Userdetailsservice,
                           Oauth2_login_success_handler oauth2Loginsuccesshandler,
                           Oauth2_login_failure_handler oauth2Loginfailurehandler) {
        this.oauth2Userdetailsservice = oauth2Userdetailsservice;
        this.oauth2Loginsuccesshandler = oauth2Loginsuccesshandler;
        this.oauth2Loginfailurehandler = oauth2Loginfailurehandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화 (API 서버의 경우)
                .authorizeHttpRequests(auth -> auth //인증,인가 설정
                        //로그인 하지않은 상태에서도 가능한
                        .requestMatchers("/", "/login", "/login-success", "/register", "/css/**", "/js/**", "/images/**").permitAll()  // 각 경로들은 인가가 별도로 설정되지않음
                        .requestMatchers("/api/**").permitAll()  // api 로 시작하는 경로도 따로 인가없이 접근 허용
                        .requestMatchers("/boards/**").permitAll() // 모든 게시판 목록은 따로 인가없이 접근 허용된다.
                        .requestMatchers("/boards/{id}").permitAll() // 모든 게시판 상세내용은 따로 인가없이 접근 허용
                        .requestMatchers("/board/file/**").permitAll() // 파일 다운로드는 모두가 받을수있음
                        // 관리자 전용 경로 설정 (필요시 추가)
                        .requestMatchers("/swagger-ui/**","/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // '/admin'으로 시작하는 경로는 ADMIN 권한 필요
                        .anyRequest().authenticated()  // 위에 설정을 제외한 모든 요청은 부여받은 인가나 관리자 권한 인증이 필요함
                )
                .formLogin(login -> login
                        .loginPage("/login")  // 로그인 경로 설정
                        .defaultSuccessUrl("/login-success", true)  // 로그인 성공시 접속 경로 수정
                        .permitAll() // 로그인 페이지에 대한 접근은 모두 허용 시킴
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/boards?logout")  // 로그아웃 성공 시 경로 수정
                        .permitAll() // 로그아웃 요청은 모두 허용 시킴
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .defaultSuccessUrl("/boards?login=true", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2Userdetailsservice))
                        .successHandler(oauth2Loginsuccesshandler)
                        .failureHandler(oauth2Loginfailurehandler)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 비밀번호는 단방향 해시 알고리즘인 Bcrypt 를 이용하여 암호화
    }
}