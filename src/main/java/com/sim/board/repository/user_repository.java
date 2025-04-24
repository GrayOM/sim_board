package com.sim.board.repository;

import com.sim.board.domain.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface user_repository extends JpaRepository<user, Long> {
    Optional<user> findByUsername(String username);  // username 으로 사용자 찾기
    boolean existsByUsername(String username);  // username 존재 여부를 참거짓 확인
    boolean existsByEmail(String email);  // email 존재 여부를 참/거짓 확인
}