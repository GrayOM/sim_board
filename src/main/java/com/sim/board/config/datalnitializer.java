package com.sim.board.config;

import com.sim.board.domain.user;
import com.sim.board.service.user_service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class datalnitializer implements CommandLineRunner {
//기본 관리자 설정하는 클래스
    private final user_service userService;

    @Override
    public void run(String... args) {
        // 관리자 계정이 있는지 확인
        if (!userService.existsByUsername("admin")) {
            // 관리자 계정 생성
            user admin = user.builder()
                    .username("admin")
                    .password("admin") // 암호화는 서비스에서 처리
                    .name("관리자")
                    .email("admin@example.com")
                    .build();

            userService.registerAdmin(admin);
            System.out.println("관리자 계정이 생성되었습니다. (ID: admin, PW: admin)");
        }
    }
}