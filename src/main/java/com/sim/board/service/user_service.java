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

    // user_service.java 수정
// user 등록 부분만 수정
    @Transactional
    public user register(user user) {
        // 입력값 살균
        user.setUsername(sanitizerService.sanitizeStrict(user.getUsername()));
        user.setName(sanitizerService.sanitizeStrict(user.getName()));
        user.setEmail(sanitizerService.sanitizeStrict(user.getEmail()));

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

        // 비밀번호 변경 시간 설정
        user.setPasswordChangedAt(LocalDateTime.now());

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
        // 먼저 username으로 조회 시도
        Optional<user> userByUsername = userRepository.findByUsername(username);
        if (userByUsername.isPresent()) {
            return userByUsername.get();
        }

        // username으로 찾지 못한 경우 이메일로 조회 시도 (소셜 로그인 사용자)
        Optional<user> userByEmail = userRepository.findByEmail(username);
        if (userByEmail.isPresent()) {
            return userByEmail.get();
        }

        // 디버깅 로그 추가
        System.out.println("사용자를 찾을 수 없음 - 검색 키워드: " + username);
        System.out.println("DB 내 모든 사용자 목록:");
        userRepository.findAll().forEach(u -> System.out.println("ID: " + u.getId() + ", Username: " + u.getUsername() +
                ", Email: " + u.getEmail() + ", Role: " + u.getRole() +
                ", Provider: " + u.getProvider()));

        throw new RuntimeException("사용자를 찾을 수 없습니다: " + username);
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

    // 사용자 정보 업데이트
    @Transactional
    public user updateUser(user user) {
        return userRepository.save(user);
    }
}