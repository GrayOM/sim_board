package com.sim.board.service;

import com.sim.board.domain.user;
import com.sim.board.repository.user_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class user_service {

    private final user_repository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public user register(user user) {
        // 사용자명과 이메일 중복 검사
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("이미 사용 중인 사용자명입니다.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 기본 권한 설정
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }

    // 사용자 정보 조회
    @Transactional(readOnly = true)
    public user getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}