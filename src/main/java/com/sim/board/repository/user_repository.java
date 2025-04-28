package com.sim.board.repository;

import com.sim.board.domain.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface user_repository extends JpaRepository<user, Long> {
    Optional<user> findByUsername(String username);  // username 으로 사용자 찾기

    //회원가입 중복 검사용
    boolean existsByUsername(String username);  // username 존재 여부 확인
    boolean existsByEmail(String email);  // email 존재 여부  확인
    boolean existsByName(String name); // 이름 여부 존재 여부 확인
}