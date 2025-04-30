package com.sim.board.service;

import com.sim.board.domain.user;
import com.sim.board.repository.user_repository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByName(user.getName())) {
            throw new RuntimeException("이미 사용 중인 이름입니다.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 기본 권한 설정
        user.setRole(com.sim.board.domain.user.ROLE_USER);

        return userRepository.save(user);
    }

    // 관리자 계정 생성 (초기 데이터용)
    @Transactional
    public user registerAdmin(user user) {
        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 관리자 권한 설정
        user.setRole(com.sim.board.domain.user.ROLE_ADMIN);

        return userRepository.save(user);
    }

    // 사용자 정보 조회 (username으로)
    @Transactional(readOnly = true)
    public user getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 사용자 정보 조회 (이메일로)
    @Transactional(readOnly = true)
    public Optional<user> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 사용자명으로 존재 여부 확인
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // 소셜 로그인 사용자 업데이트 또는 생성
    @Transactional
    public user processOAuthUser(String provider, String providerId, String email, String name, String profileImageUrl) {
        Optional<user> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // 기존 사용자 업데이트
            user userEntity = existingUser.get();

            // 소셜 정보 업데이트 (이미 설정되어 있지 않은 경우)
            if (userEntity.getProvider() == null || !userEntity.getProvider().equals(provider)) {
                userEntity.setProvider(provider);
                userEntity.setProviderId(providerId);
                userEntity.setProfileImageUrl(profileImageUrl);
                userEntity = userRepository.save(userEntity);
            }

            return userEntity;
        } else {
            // 새 사용자 생성
            user newUser = user.builder()
                    .username(email) // 이메일을 사용자명으로 사용
                    .email(email)
                    .name(name)
                    .password(passwordEncoder.encode("oauth2user")) // 실제 비밀번호는 사용하지 않음
                    .provider(provider)
                    .providerId(providerId)
                    .profileImageUrl(profileImageUrl)
                    .role(user.ROLE_USER) // 기본 역할은 USER
                    .build();

            return userRepository.save(newUser);
        }
    }
}