// src/main/java/com/sim/board/config/security_config.java
package com.sim.board.config;

import com.sim.board.config.oauth.Oauth2_user_details_service;
import com.sim.board.config.oauth.Oauth2_login_failure_handler;
import com.sim.board.config.oauth.Oauth2_login_success_handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class security_config {

    private final Oauth2_user_details_service oauth2Userdetailsservice;
    private final Oauth2_login_success_handler oauth2Loginsuccesshandler;
    private final Oauth2_login_failure_handler oauth2Loginfailurehandler;
    private final user_db_connection userDbConnection;
    private final LoginAttemptService loginAttemptService;
    private final CustomAuthenticationSuccessHandler authSuccessHandler;
    private final CustomAuthenticationFailureHandler authFailureHandler;

    public security_config(Oauth2_user_details_service oauth2Userdetailsservice,
                           Oauth2_login_success_handler oauth2Loginsuccesshandler,
                           Oauth2_login_failure_handler oauth2Loginfailurehandler,
                           user_db_connection userDbConnection,
                           LoginAttemptService loginAttemptService,
                           CustomAuthenticationSuccessHandler authSuccessHandler,
                           CustomAuthenticationFailureHandler authFailureHandler) {
        this.oauth2Userdetailsservice = oauth2Userdetailsservice;
        this.oauth2Loginsuccesshandler = oauth2Loginsuccesshandler;
        this.oauth2Loginfailurehandler = oauth2Loginfailurehandler;
        this.userDbConnection = userDbConnection;
        this.loginAttemptService = loginAttemptService;
        this.authSuccessHandler = authSuccessHandler;
        this.authFailureHandler = authFailureHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/login-success", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/boards").permitAll()
                        .requestMatchers("/boards/{id}").permitAll()
                        .requestMatchers("/board/file/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/swagger-ui/**","/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .failureHandler(authFailureHandler)
                        .successHandler(authSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/boards?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .defaultSuccessUrl("/boards?login=true", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2Userdetailsservice))
                        .successHandler(oauth2Loginsuccesshandler)
                        .failureHandler(oauth2Loginfailurehandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomAuthenticationFilter authenticationFilter(AuthenticationManager authenticationManager) {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(authenticationManager, loginAttemptService);
        filter.setAuthenticationSuccessHandler(authSuccessHandler);
        filter.setAuthenticationFailureHandler(authFailureHandler);
        return filter;
    }
}