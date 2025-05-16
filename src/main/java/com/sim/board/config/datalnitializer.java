package com.sim.board.config;

import com.sim.board.domain.user;
import com.sim.board.service.user_service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class datalnitializer implements CommandLineRunner {

    private final user_service userService;
    private static final Logger logger = Logger.getLogger(datalnitializer.class.getName());

    @Override
    public void run(String... args) {
        // 관리자 계정이 있는지 확인
        if (!userService.existsByUsername("admin")) {
            // 강력한 무작위 비밀번호 생성
            String securePassword = generateSecurePassword();

            // 관리자 계정 생성
            user admin = user.builder()
                    .username("admin")
                    .password(securePassword) // 암호화는 서비스에서 처리
                    .name("관리자")
                    .email("admin@example.com")
                    .passwordChangedAt(LocalDateTime.now())
                    .build();

            userService.registerAdmin(admin);

            // 중요: 실제 환경에서는 로그에 비밀번호를 출력하지 않는 것이 좋습니다
            // 개발 환경에서만 다음 로그를 사용하세요
            logger.warning("========================================");
            logger.warning("관리자 계정이 생성되었습니다.");
            logger.warning("사용자명: admin");
            logger.warning("비밀번호: " + securePassword);
            logger.warning("*** 이 비밀번호를 안전한 곳에 보관하세요. 다시 표시되지 않습니다 ***");
            logger.warning("========================================");
        }
    }

    // 강력한 비밀번호 생성 메소드 (최소 16자리, 다양한 문자 조합)
    private String generateSecurePassword() {
        final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        final String NUMBER = "0123456789";
        final String SPECIAL = "!@#$%^&*()_-+=<>?";

        final String ALL_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(16); // 16자리 비밀번호

        // 각 유형별로 적어도 하나의 문자를 포함
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // 나머지 문자 추가
        for (int i = 4; i < 16; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // 문자열 섞기
        char[] passArray = password.toString().toCharArray();
        for (int i = 0; i < passArray.length; i++) {
            int j = random.nextInt(passArray.length);
            char temp = passArray[i];
            passArray[i] = passArray[j];
            passArray[j] = temp;
        }

        return new String(passArray);
    }
}